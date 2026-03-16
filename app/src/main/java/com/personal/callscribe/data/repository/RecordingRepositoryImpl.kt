package com.personal.callscribe.data.repository

import com.personal.callscribe.data.local.db.RecordingDao
import com.personal.callscribe.data.local.filesystem.AudioFileManager
import com.personal.callscribe.data.mapper.RecordingMapper
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.repository.IRecordingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Default recording repository backed by Room and app-private files.
 */
class RecordingRepositoryImpl(
    private val recordingDao: RecordingDao,
    private val audioFileManager: AudioFileManager,
) : IRecordingRepository {

    override fun observeRecordings(): Flow<List<Recording>> = recordingDao.observeAll()
        .map { entities -> entities.map(RecordingMapper::toDomain) }

    override suspend fun getRecording(recordingId: Long): AppResult<Recording> {
        val entity = recordingDao.getById(recordingId)
            ?: return AppResult.Failure(AppError.FileNotFound)
        return AppResult.Success(RecordingMapper.toDomain(entity))
    }

    override suspend fun insertRecording(recording: Recording): AppResult<Long> = try {
        AppResult.Success(recordingDao.insert(RecordingMapper.toEntity(recording)))
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unexpected(throwable))
    }

    override suspend fun renameRecording(recordingId: Long, newTitle: String): AppResult<Unit> {
        val sanitizedTitle = newTitle.trim()
        if (sanitizedTitle.isBlank()) {
            return AppResult.Failure(AppError.Unexpected(IllegalArgumentException("Title must not be blank.")))
        }
        return try {
            if (recordingDao.updateTitle(recordingId, sanitizedTitle) > 0) {
                AppResult.Success(Unit)
            } else {
                AppResult.Failure(AppError.FileNotFound)
            }
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unexpected(throwable))
        }
    }

    override suspend fun deleteRecording(recordingId: Long): AppResult<Unit> {
        val existing = recordingDao.getById(recordingId)
            ?: return AppResult.Failure(AppError.FileNotFound)
        val fileDeletion = audioFileManager.deleteFile(existing.filePath)
        if (fileDeletion is AppResult.Failure) {
            return fileDeletion
        }
        return try {
            if (recordingDao.deleteById(recordingId) > 0) {
                AppResult.Success(Unit)
            } else {
                AppResult.Failure(AppError.FileNotFound)
            }
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unexpected(throwable))
        }
    }

    override suspend fun exportRecording(recordingId: Long): AppResult<String> {
        val existing = recordingDao.getById(recordingId)
            ?: return AppResult.Failure(AppError.FileNotFound)
        return if (audioFileManager.exists(existing.filePath)) {
            AppResult.Success(existing.filePath)
        } else {
            AppResult.Failure(AppError.FileReadError(existing.filePath))
        }
    }
}
