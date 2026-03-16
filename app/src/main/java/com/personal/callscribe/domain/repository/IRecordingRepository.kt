package com.personal.callscribe.domain.repository

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.Recording
import kotlinx.coroutines.flow.Flow

/**
 * Recording persistence and file lifecycle contract.
 */
interface IRecordingRepository {
    fun observeRecordings(): Flow<List<Recording>>
    suspend fun getRecording(recordingId: Long): AppResult<Recording>
    suspend fun insertRecording(recording: Recording): AppResult<Long>
    suspend fun renameRecording(recordingId: Long, newTitle: String): AppResult<Unit>
    suspend fun deleteRecording(recordingId: Long): AppResult<Unit>
    suspend fun exportRecording(recordingId: Long): AppResult<String>
}
