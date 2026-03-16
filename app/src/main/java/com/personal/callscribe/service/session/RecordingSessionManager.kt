package com.personal.callscribe.service.session

import com.personal.callscribe.data.local.filesystem.AudioFileManager
import com.personal.callscribe.domain.audio.IAudioFileWriter
import com.personal.callscribe.domain.audio.IAudioLevelMonitor
import com.personal.callscribe.domain.audio.IAudioProcessor
import com.personal.callscribe.domain.audio.IAudioRecorder
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.domain.model.AudioConfig
import com.personal.callscribe.domain.model.AudioLevel
import com.personal.callscribe.domain.model.CalibrationResult
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.model.RecordingSession
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.domain.phone.ICallStateMonitor
import com.personal.callscribe.domain.phone.IRecordingSessionController
import com.personal.callscribe.domain.phone.ISpeakerphoneController
import com.personal.callscribe.domain.repository.IDiagnosticsRepository
import com.personal.callscribe.domain.repository.IRecordingRepository
import com.personal.callscribe.domain.repository.ISettingsRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.ArrayDeque
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central orchestrator implementing the formal recording session state machine.
 */
class RecordingSessionManager(
    private val audioRecorder: IAudioRecorder,
    private val audioLevelMonitor: IAudioLevelMonitor,
    private val audioFileWriter: IAudioFileWriter,
    private val audioProcessor: IAudioProcessor,
    private val speakerphoneController: ISpeakerphoneController,
    private val callStateMonitor: ICallStateMonitor,
    private val recordingRepository: IRecordingRepository,
    private val settingsRepository: ISettingsRepository,
    private val diagnosticsRepository: IDiagnosticsRepository,
    private val audioFileManager: AudioFileManager,
) : IRecordingSessionController {
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val stateMutex = Mutex()
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.FRANCE)
        .withZone(ZoneId.systemDefault())

    private val mutableSessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    private val recentLevels = ArrayDeque<AudioLevel>()
    private var currentSession: RecordingSession? = null
    private var currentSettings: AppSettings = AppSettings()

    override val sessionState: StateFlow<SessionState> = mutableSessionState.asStateFlow()
    override val callState: StateFlow<CallState> = callStateMonitor.callState

    override suspend fun startSession(): AppResult<RecordingSession> = stateMutex.withLock {
        if (mutableSessionState.value != SessionState.Idle) {
            return AppResult.Failure(AppError.AudioSessionAlreadyActive)
        }

        currentSettings = loadSettingsOrDefault()
        mutableSessionState.value = SessionState.Preparing("Préparation du microphone")

        val createdFile = when (val fileResult = audioFileManager.createRecordingFile(currentSettings.preferredFilePrefix)) {
            is AppResult.Success -> fileResult.data
            is AppResult.Failure -> {
                mutableSessionState.value = SessionState.Error(fileResult.error)
                return fileResult
            }
        }

        val initialSession = RecordingSession(
            sessionId = UUID.randomUUID().toString(),
            outputFilePath = createdFile.absolutePath,
            config = AudioConfig(sampleRateHz = currentSettings.preferredSampleRateHz),
            startedAtMillis = System.currentTimeMillis(),
            speakerphoneRequested = currentSettings.speakerphoneEnabled,
            speakerphoneActivated = false,
        )
        currentSession = initialSession
        recentLevels.clear()

        when (val prepareResult = audioRecorder.prepare(initialSession.config)) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> {
                cleanupFailedStart(createdFile.absolutePath)
                mutableSessionState.value = SessionState.Error(prepareResult.error)
                logDiagnostic(DiagnosticLevel.ERROR, "SessionManager", "Microphone initialization failed.", prepareResult.error.toString())
                return prepareResult
            }
        }

        when (val writerResult = audioFileWriter.open(createdFile.absolutePath, initialSession.config)) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> {
                cleanupFailedStart(createdFile.absolutePath)
                mutableSessionState.value = SessionState.Error(writerResult.error)
                logDiagnostic(DiagnosticLevel.ERROR, "SessionManager", "WAV writer initialization failed.", writerResult.error.toString())
                return writerResult
            }
        }

        mutableSessionState.value = SessionState.EnablingSpeaker(initialSession)

        val speakerActivated = if (currentSettings.speakerphoneEnabled) {
            when (val speakerResult = speakerphoneController.requestSpeakerphone(true)) {
                is AppResult.Success -> speakerResult.data
                is AppResult.Failure -> {
                    logDiagnostic(
                        DiagnosticLevel.WARNING,
                        "SessionManager",
                        "Speakerphone activation failed; recording continues without it.",
                        speakerResult.error.toString(),
                    )
                    false
                }
            }
        } else {
            false
        }

        val activeSession = initialSession.copy(speakerphoneActivated = speakerActivated)
        currentSession = activeSession

        when (
            val startResult = audioRecorder.start(
                onAudioChunk = { buffer, bytesRead ->
                    val processed = audioProcessor.process(buffer, bytesRead, currentSettings.inputGainPercent)
                    when (val writeResult = audioFileWriter.write(processed, processed.size)) {
                        is AppResult.Success -> {
                            val level = audioLevelMonitor.computeLevel(processed, processed.size)
                            synchronized(recentLevels) {
                                recentLevels.addLast(level)
                                while (recentLevels.size > 30) {
                                    recentLevels.removeFirst()
                                }
                            }
                            val currentState = mutableSessionState.value
                            if (currentState is SessionState.Recording &&
                                currentState.session.sessionId == activeSession.sessionId
                            ) {
                                mutableSessionState.value = currentState.copy(
                                    audioLevel = level,
                                    elapsedMs = System.currentTimeMillis() - activeSession.startedAtMillis,
                                )
                            }
                        }

                        is AppResult.Failure -> controllerScope.launch {
                            abortSession(writeResult.error, "Audio file write failed.")
                        }
                    }
                },
                onError = { error ->
                    controllerScope.launch {
                        abortSession(error, "Audio capture loop failed.")
                    }
                },
            )
        ) {
            is AppResult.Success -> {
                mutableSessionState.value = SessionState.Recording(
                    session = activeSession,
                    audioLevel = AudioLevel.Silent,
                    elapsedMs = 0L,
                )
                logDiagnostic(
                    DiagnosticLevel.INFO,
                    "SessionManager",
                    "Recording session started.",
                    "speakerActivated=$speakerActivated file=${activeSession.outputFilePath}",
                )
                AppResult.Success(activeSession)
            }

            is AppResult.Failure -> {
                cleanupFailedStart(createdFile.absolutePath)
                mutableSessionState.value = SessionState.Error(startResult.error)
                logDiagnostic(
                    DiagnosticLevel.ERROR,
                    "SessionManager",
                    "Audio capture start failed.",
                    startResult.error.toString(),
                )
                startResult
            }
        }
    }

    override suspend fun stopSession(): AppResult<Recording> = stateMutex.withLock {
        val currentState = mutableSessionState.value as? SessionState.Recording
            ?: return AppResult.Failure(AppError.Unexpected(IllegalStateException("No active recording session.")))

        val session = currentState.session
        mutableSessionState.value = SessionState.Finalizing(session)

        val stopResult = audioRecorder.stop()
        audioRecorder.release()
        speakerphoneController.requestSpeakerphone(false)

        if (stopResult is AppResult.Failure) {
            mutableSessionState.value = SessionState.Error(stopResult.error)
            logDiagnostic(DiagnosticLevel.ERROR, "SessionManager", "Audio recorder stop failed.", stopResult.error.toString())
            return stopResult
        }

        val closeResult = audioFileWriter.close()
        val bytesWritten = when (closeResult) {
            is AppResult.Success -> closeResult.data
            is AppResult.Failure -> {
                mutableSessionState.value = SessionState.Error(closeResult.error)
                logDiagnostic(DiagnosticLevel.ERROR, "SessionManager", "WAV finalization failed.", closeResult.error.toString())
                return closeResult
            }
        }

        val persisted = Recording(
            title = buildRecordingTitle(session.startedAtMillis),
            fileName = audioFileManager.fileName(session.outputFilePath),
            filePath = session.outputFilePath,
            durationMs = (System.currentTimeMillis() - session.startedAtMillis).coerceAtLeast(0L),
            sizeBytes = maxOf(audioFileManager.fileSize(session.outputFilePath), bytesWritten + 44L),
            createdAtMillis = session.startedAtMillis,
            sampleRateHz = session.config.sampleRateHz,
            channelCount = session.config.channelCount,
            speakerphoneRequested = session.speakerphoneRequested,
            speakerphoneActivated = session.speakerphoneActivated,
        )

        val insertResult = recordingRepository.insertRecording(persisted)
        val recording = when (insertResult) {
            is AppResult.Success -> persisted.copy(id = insertResult.data)
            is AppResult.Failure -> {
                mutableSessionState.value = SessionState.Error(insertResult.error)
                logDiagnostic(
                    DiagnosticLevel.ERROR,
                    "SessionManager",
                    "Recording metadata persistence failed.",
                    insertResult.error.toString(),
                )
                return insertResult
            }
        }

        currentSession = null
        recentLevels.clear()
        mutableSessionState.value = SessionState.Completed(recording)
        logDiagnostic(
            DiagnosticLevel.INFO,
            "SessionManager",
            "Recording session completed.",
            "recordingId=${recording.id} path=${recording.filePath}",
        )
        AppResult.Success(recording)
    }

    override suspend fun runCalibration(durationMs: Long): AppResult<CalibrationResult> = stateMutex.withLock {
        if (mutableSessionState.value is SessionState.Recording) {
            val levels = synchronized(recentLevels) { recentLevels.toList() }
            return AppResult.Success(buildCalibrationResult(levels))
        }
        if (mutableSessionState.value !is SessionState.Idle &&
            mutableSessionState.value !is SessionState.Completed &&
            mutableSessionState.value !is SessionState.Error
        ) {
            return AppResult.Failure(AppError.AudioSessionAlreadyActive)
        }
        if (mutableSessionState.value is SessionState.Completed || mutableSessionState.value is SessionState.Error) {
            reset()
        }

        val settings = loadSettingsOrDefault()
        val calibrationLevels = mutableListOf<AudioLevel>()
        val calibrationError = AtomicReference<AppError?>(null)
        val config = AudioConfig(sampleRateHz = settings.preferredSampleRateHz)

        when (val prepareResult = audioRecorder.prepare(config)) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> return prepareResult
        }

        when (
            val startResult = audioRecorder.start(
                onAudioChunk = { buffer, bytesRead ->
                    calibrationLevels += audioLevelMonitor.computeLevel(buffer, bytesRead)
                },
                onError = { error ->
                    calibrationError.compareAndSet(null, error)
                },
            )
        ) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> {
                audioRecorder.release()
                return startResult
            }
        }

        delay(durationMs.coerceAtLeast(500L))
        val stopResult = audioRecorder.stop()
        audioRecorder.release()

        calibrationError.get()?.let { return AppResult.Failure(it) }
        if (stopResult is AppResult.Failure) {
            return stopResult
        }

        return AppResult.Success(buildCalibrationResult(calibrationLevels))
    }

    override fun reset() {
        if (mutableSessionState.value is SessionState.Recording) {
            return
        }
        currentSession = null
        recentLevels.clear()
        mutableSessionState.value = SessionState.Idle
    }

    private suspend fun abortSession(error: AppError, message: String) {
        stateMutex.withLock {
            if (mutableSessionState.value !is SessionState.Recording &&
                mutableSessionState.value !is SessionState.EnablingSpeaker
            ) {
                return
            }
            runCatching { audioRecorder.stop() }
            audioRecorder.release()
            runCatching { audioFileWriter.close() }
            runCatching { audioFileWriter.deleteCurrentFile() }
            runCatching { speakerphoneController.requestSpeakerphone(false) }
            currentSession = null
            recentLevels.clear()
            mutableSessionState.value = SessionState.Error(error)
            logDiagnostic(DiagnosticLevel.ERROR, "SessionManager", message, error.toString())
        }
    }

    private suspend fun cleanupFailedStart(path: String) {
        runCatching { audioRecorder.stop() }
        audioRecorder.release()
        runCatching { audioFileWriter.deleteCurrentFile() }
        runCatching { audioFileManager.deleteFile(path) }
        runCatching { speakerphoneController.requestSpeakerphone(false) }
        currentSession = null
        recentLevels.clear()
    }

    private suspend fun loadSettingsOrDefault(): AppSettings = when (val settingsResult = settingsRepository.getSettings()) {
        is AppResult.Success -> settingsResult.data
        is AppResult.Failure -> AppSettings()
    }

    private fun buildCalibrationResult(levels: List<AudioLevel>): CalibrationResult {
        val averageDb = if (levels.isEmpty()) -50.0 else levels.map(AudioLevel::decibels).average()
        val (gain, speaker, recommendation) = when {
            averageDb < -42.0 -> Triple(150, 95, "Signal faible. Montez le volume haut-parleur et rapprochez le téléphone.")
            averageDb < -34.0 -> Triple(125, 85, "Signal modéré. Un léger gain logiciel améliorera la transcription.")
            averageDb < -26.0 -> Triple(110, 75, "Signal correct. Le réglage actuel est exploitable.")
            else -> Triple(95, 60, "Signal élevé. Réduisez légèrement le volume haut-parleur pour éviter la saturation.")
        }
        return CalibrationResult(
            averageDecibels = averageDb,
            recommendedGainPercent = gain,
            recommendedSpeakerVolumePercent = speaker,
            recommendation = recommendation,
        )
    }

    private fun buildRecordingTitle(timestampMillis: Long): String =
        "Appel ${formatter.format(Instant.ofEpochMilli(timestampMillis))}"

    private suspend fun logDiagnostic(
        level: DiagnosticLevel,
        tag: String,
        message: String,
        details: String? = null,
    ) {
        if (!currentSettings.enableDiagnostics && level != DiagnosticLevel.ERROR) {
            return
        }
        diagnosticsRepository.log(level, tag, message, details)
    }
}
