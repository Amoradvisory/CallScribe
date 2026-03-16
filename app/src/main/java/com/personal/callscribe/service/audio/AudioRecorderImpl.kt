package com.personal.callscribe.service.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.personal.callscribe.domain.audio.IAudioRecorder
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AudioConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Android AudioRecord implementation for ambient microphone capture.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudioRecorderImpl : IAudioRecorder {
    private val recorderScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private var audioRecord: AudioRecord? = null
    private var readJob: Job? = null
    private var bufferSizeInBytes: Int = 0

    override val isRecording: Boolean
        get() = audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    override suspend fun prepare(config: AudioConfig): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            release()
            val channelMask = if (config.channelCount == 1) {
                AudioFormat.CHANNEL_IN_MONO
            } else {
                AudioFormat.CHANNEL_IN_STEREO
            }
            val minBuffer = AudioRecord.getMinBufferSize(
                config.sampleRateHz,
                channelMask,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minBuffer <= 0) {
                return@withContext AppResult.Failure(AppError.AudioRecordInitFailed)
            }
            bufferSizeInBytes = maxOf(config.bufferSizeInBytes, minBuffer * 2)
            val createdRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(config.sampleRateHz)
                        .setChannelMask(channelMask)
                        .build(),
                )
                .setBufferSizeInBytes(bufferSizeInBytes)
                .build()

            if (createdRecorder.state != AudioRecord.STATE_INITIALIZED) {
                createdRecorder.release()
                return@withContext AppResult.Failure(AppError.AudioRecordInitFailed)
            }
            audioRecord = createdRecorder
            AppResult.Success(Unit)
        } catch (securityException: SecurityException) {
            AppResult.Failure(AppError.PermissionDenied("android.permission.RECORD_AUDIO"))
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unexpected(throwable))
        }
    }

    override suspend fun start(
        onAudioChunk: suspend (ByteArray, Int) -> Unit,
        onError: suspend (AppError) -> Unit,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        val recorder = audioRecord ?: return@withContext AppResult.Failure(AppError.AudioRecordInitFailed)
        if (readJob?.isActive == true || recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return@withContext AppResult.Failure(AppError.AudioSessionAlreadyActive)
        }
        return@withContext try {
            recorder.startRecording()
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                return@withContext AppResult.Failure(AppError.MicrophoneUnavailable)
            }
            readJob = recorderScope.launch {
                val buffer = ByteArray(bufferSizeInBytes)
                while (true) {
                    val bytesRead = recorder.read(buffer, 0, buffer.size)
                    when {
                        bytesRead > 0 -> onAudioChunk(buffer.copyOf(bytesRead), bytesRead)
                        bytesRead == 0 -> Unit
                        bytesRead == AudioRecord.ERROR_INVALID_OPERATION ->
                            onError(AppError.AudioRecordError(bytesRead))

                        bytesRead == AudioRecord.ERROR_BAD_VALUE ->
                            onError(AppError.AudioRecordError(bytesRead))

                        else -> break
                    }
                    if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                        break
                    }
                }
            }
            AppResult.Success(Unit)
        } catch (securityException: SecurityException) {
            AppResult.Failure(AppError.PermissionDenied("android.permission.RECORD_AUDIO"))
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unexpected(throwable))
        }
    }

    override suspend fun stop(): AppResult<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            audioRecord?.let { recorder ->
                if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop()
                }
            }
            readJob?.cancelAndJoin()
            readJob = null
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unexpected(throwable))
        }
    }

    override fun release() {
        runCatching { audioRecord?.release() }
        audioRecord = null
        readJob = null
        bufferSizeInBytes = 0
    }
}
