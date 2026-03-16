package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.repository.IRecordingRepository

/**
 * Deletes a recording and its underlying file.
 */
class DeleteRecordingUseCase(
    private val recordingRepository: IRecordingRepository,
) {
    suspend operator fun invoke(recordingId: Long): AppResult<Unit> =
        recordingRepository.deleteRecording(recordingId)
}
