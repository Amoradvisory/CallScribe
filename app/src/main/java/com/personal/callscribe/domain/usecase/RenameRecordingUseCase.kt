package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.repository.IRecordingRepository

/**
 * Renames a persisted recording.
 */
class RenameRecordingUseCase(
    private val recordingRepository: IRecordingRepository,
) {
    suspend operator fun invoke(recordingId: Long, newTitle: String): AppResult<Unit> =
        recordingRepository.renameRecording(recordingId, newTitle.trim())
}
