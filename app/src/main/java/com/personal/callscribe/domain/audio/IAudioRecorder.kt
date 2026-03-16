package com.personal.callscribe.domain.audio

import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AudioConfig

/**
 * Abstraction over live microphone capture.
 */
interface IAudioRecorder {
    val isRecording: Boolean
    suspend fun prepare(config: AudioConfig): AppResult<Unit>
    suspend fun start(
        onAudioChunk: suspend (ByteArray, Int) -> Unit,
        onError: suspend (AppError) -> Unit,
    ): AppResult<Unit>

    suspend fun stop(): AppResult<Unit>
    fun release()
}
