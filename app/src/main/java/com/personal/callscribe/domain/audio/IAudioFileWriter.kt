package com.personal.callscribe.domain.audio

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AudioConfig

/**
 * Writes a PCM stream to a final audio file.
 */
interface IAudioFileWriter {
    suspend fun open(filePath: String, config: AudioConfig): AppResult<Unit>
    suspend fun write(buffer: ByteArray, bytesRead: Int): AppResult<Unit>
    suspend fun close(): AppResult<Long>
    suspend fun deleteCurrentFile(): AppResult<Unit>
}
