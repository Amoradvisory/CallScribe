package com.personal.callscribe.data.local.filesystem

import android.content.Context
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Handles on-disk recording files stored in app-private storage.
 */
class AudioFileManager(
    private val context: Context,
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US)

    fun createRecordingFile(prefix: String): AppResult<File> {
        val recordingsDir = File(context.filesDir, "recordings")
        if (!recordingsDir.exists() && !recordingsDir.mkdirs()) {
            return AppResult.Failure(AppError.StorageUnavailable)
        }
        val safePrefix = prefix.ifBlank { "CallScribe" }
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .take(32)
            .ifBlank { "CallScribe" }
        val timestamp = LocalDateTime.now().format(formatter)
        val file = File(recordingsDir, "${safePrefix}_${timestamp}.wav")
        return try {
            if (file.exists().not() && file.createNewFile()) {
                AppResult.Success(file)
            } else if (file.exists()) {
                AppResult.Success(file)
            } else {
                AppResult.Failure(AppError.StorageUnavailable)
            }
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.FileWriteError(throwable))
        }
    }

    fun deleteFile(path: String): AppResult<Unit> {
        val file = File(path)
        return if (!file.exists()) {
            AppResult.Success(Unit)
        } else if (file.delete()) {
            AppResult.Success(Unit)
        } else {
            AppResult.Failure(AppError.FileWriteError(IllegalStateException("Unable to delete $path")))
        }
    }

    fun fileSize(path: String): Long = File(path).takeIf(File::exists)?.length() ?: 0L

    fun fileName(path: String): String = File(path).name

    fun exists(path: String): Boolean = File(path).exists()
}
