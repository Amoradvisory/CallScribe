package com.personal.callscribe.di

import android.content.Context
import androidx.room.Room
import com.personal.callscribe.data.local.datastore.SettingsDataStore
import com.personal.callscribe.data.local.db.CallScribeDatabase
import com.personal.callscribe.data.local.filesystem.AudioFileManager
import com.personal.callscribe.data.repository.DiagnosticsRepositoryImpl
import com.personal.callscribe.data.repository.RecordingRepositoryImpl
import com.personal.callscribe.data.repository.SettingsRepositoryImpl
import com.personal.callscribe.domain.phone.IRecordingSessionController
import com.personal.callscribe.domain.usecase.ClearDiagnosticsUseCase
import com.personal.callscribe.domain.usecase.DeleteRecordingUseCase
import com.personal.callscribe.domain.usecase.ExportRecordingUseCase
import com.personal.callscribe.domain.usecase.GetDiagnosticsUseCase
import com.personal.callscribe.domain.usecase.GetRecordingDetailsUseCase
import com.personal.callscribe.domain.usecase.GetRecordingsUseCase
import com.personal.callscribe.domain.usecase.ObserveSettingsUseCase
import com.personal.callscribe.domain.usecase.RenameRecordingUseCase
import com.personal.callscribe.domain.usecase.RunCalibrationUseCase
import com.personal.callscribe.domain.usecase.SaveSettingsUseCase
import com.personal.callscribe.domain.usecase.StartRecordingSessionUseCase
import com.personal.callscribe.domain.usecase.StopRecordingSessionUseCase
import com.personal.callscribe.service.audio.AudioLevelMonitorImpl
import com.personal.callscribe.service.audio.AudioProcessorImpl
import com.personal.callscribe.service.audio.AudioRecorderImpl
import com.personal.callscribe.service.AutoRecordingCoordinator
import com.personal.callscribe.service.audio.WavFileWriter
import com.personal.callscribe.service.phone.CallStateMonitorImpl
import com.personal.callscribe.service.phone.SpeakerphoneControllerImpl
import com.personal.callscribe.service.session.RecordingSessionManager

/**
 * Application-scoped manual DI container.
 */
class AppContainer(
    appContext: Context,
) {
    val applicationContext: Context = appContext.applicationContext

    private val database: CallScribeDatabase = Room.databaseBuilder(
        applicationContext,
        CallScribeDatabase::class.java,
        "callscribe.db",
    ).build()

    private val settingsDataStore = SettingsDataStore(applicationContext)
    private val audioFileManager = AudioFileManager(applicationContext)

    val recordingRepository = RecordingRepositoryImpl(
        recordingDao = database.recordingDao(),
        audioFileManager = audioFileManager,
    )
    val settingsRepository = SettingsRepositoryImpl(settingsDataStore)
    val diagnosticsRepository = DiagnosticsRepositoryImpl(database.diagnosticLogDao())

    val audioRecorder = AudioRecorderImpl()
    val audioLevelMonitor = AudioLevelMonitorImpl()
    val audioFileWriter = WavFileWriter()
    val audioProcessor = AudioProcessorImpl()
    val callStateMonitor = CallStateMonitorImpl(applicationContext)
    val speakerphoneController = SpeakerphoneControllerImpl(applicationContext)

    val sessionController: IRecordingSessionController = RecordingSessionManager(
        audioRecorder = audioRecorder,
        audioLevelMonitor = audioLevelMonitor,
        audioFileWriter = audioFileWriter,
        audioProcessor = audioProcessor,
        speakerphoneController = speakerphoneController,
        callStateMonitor = callStateMonitor,
        recordingRepository = recordingRepository,
        settingsRepository = settingsRepository,
        diagnosticsRepository = diagnosticsRepository,
        audioFileManager = audioFileManager,
    )

    val autoRecordingCoordinator = AutoRecordingCoordinator(
        appContext = applicationContext,
        settingsRepository = settingsRepository,
        diagnosticsRepository = diagnosticsRepository,
        sessionController = sessionController,
    )

    val startRecordingSessionUseCase = StartRecordingSessionUseCase(sessionController)
    val stopRecordingSessionUseCase = StopRecordingSessionUseCase(sessionController)
    val getRecordingsUseCase = GetRecordingsUseCase(recordingRepository)
    val deleteRecordingUseCase = DeleteRecordingUseCase(recordingRepository)
    val renameRecordingUseCase = RenameRecordingUseCase(recordingRepository)
    val getRecordingDetailsUseCase = GetRecordingDetailsUseCase(recordingRepository)
    val exportRecordingUseCase = ExportRecordingUseCase(recordingRepository)
    val runCalibrationUseCase = RunCalibrationUseCase(sessionController)
    val saveSettingsUseCase = SaveSettingsUseCase(settingsRepository)
    val observeSettingsUseCase = ObserveSettingsUseCase(settingsRepository)
    val getDiagnosticsUseCase = GetDiagnosticsUseCase(diagnosticsRepository)
    val clearDiagnosticsUseCase = ClearDiagnosticsUseCase(diagnosticsRepository)
}
