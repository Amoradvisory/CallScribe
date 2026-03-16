package com.personal.callscribe.service.audio

import com.personal.callscribe.domain.audio.IAudioFileWriter
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AudioConfig
import java.io.File
import java.io.RandomAccessFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Writes WAV files with a finalized header once the stream closes.
 */
class WavFileWriter : IAudioFileWriter {
    private var randomAccessFile: RandomAccessFile? = null
    private var currentConfig: AudioConfig? = null
    private var currentFilePath: String? = null
    private var pcmBytesWritten: Long = 0L

    override suspend fun open(filePath: String, config: AudioConfig): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            randomAccessFile?.close()
            randomAccessFile = RandomAccessFile(File(filePath), "rw").apply {
                setLength(0L)
                write(ByteArray(44))
            }
            currentConfig = config
            currentFilePath = filePath
            pcmBytesWritten = 0L
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.FileWriteError(throwable))
        }
    }

    override suspend fun write(buffer: ByteArray, bytesRead: Int): AppResult<Unit> = withContext(Dispatchers.IO) {
        val writer = randomAccessFile
            ?: return@withContext AppResult.Failure(
                AppError.FileWriteError(IllegalStateException("WAV writer is not open.")),
            )
        return@withContext try {
            writer.write(buffer, 0, bytesRead)
            pcmBytesWritten += bytesRead
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.FileWriteError(throwable))
        }
    }

    override suspend fun close(): AppResult<Long> = withContext(Dispatchers.IO) {
        val writer = randomAccessFile ?: return@withContext AppResult.Success(0L)
        val config = currentConfig
            ?: return@withContext AppResult.Failure(
                AppError.FileWriteError(IllegalStateException("Audio configuration missing.")),
            )
        return@withContext try {
            writeWavHeader(writer, config, pcmBytesWritten)
            writer.close()
            randomAccessFile = null
            currentConfig = null
            currentFilePath = null
            AppResult.Success(pcmBytesWritten)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.FileWriteError(throwable))
        }
    }

    override suspend fun deleteCurrentFile(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            randomAccessFile?.close()
            randomAccessFile = null
            val filePath = currentFilePath
            currentConfig = null
            currentFilePath = null
            pcmBytesWritten = 0L
            filePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.FileWriteError(throwable))
        }
    }

    private fun writeWavHeader(
        writer: RandomAccessFile,
        config: AudioConfig,
        pcmBytes: Long,
    ) {
        val channels = config.channelCount.coerceAtLeast(1)
        val bitsPerSample = config.bitsPerSample.coerceAtLeast(16)
        val byteRate = config.sampleRateHz * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        writer.seek(0L)
        writer.writeBytes("RIFF")
        writeLittleEndianInt(writer, (36L + pcmBytes).toInt())
        writer.writeBytes("WAVE")
        writer.writeBytes("fmt ")
        writeLittleEndianInt(writer, 16)
        writeLittleEndianShort(writer, 1.toShort())
        writeLittleEndianShort(writer, channels.toShort())
        writeLittleEndianInt(writer, config.sampleRateHz)
        writeLittleEndianInt(writer, byteRate)
        writeLittleEndianShort(writer, blockAlign.toShort())
        writeLittleEndianShort(writer, bitsPerSample.toShort())
        writer.writeBytes("data")
        writeLittleEndianInt(writer, pcmBytes.toInt())
    }

    private fun writeLittleEndianInt(writer: RandomAccessFile, value: Int) {
        writer.write(value and 0xFF)
        writer.write((value shr 8) and 0xFF)
        writer.write((value shr 16) and 0xFF)
        writer.write((value shr 24) and 0xFF)
    }

    private fun writeLittleEndianShort(writer: RandomAccessFile, value: Short) {
        writer.write(value.toInt() and 0xFF)
        writer.write((value.toInt() shr 8) and 0xFF)
    }
}
