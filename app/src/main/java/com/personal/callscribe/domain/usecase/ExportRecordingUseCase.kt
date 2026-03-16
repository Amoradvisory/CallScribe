package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.repository.IRecordingRepository

/**
 * Resolves an existing recording file path for export or sharing.
 */
class ExportRecordingUseCase(
    private val recordingRepository: IRecordingRepository,
) {
    suspend operator fun invoke(recordingId: Long): AppResult<String> =
        recordingRepository.exportRecording(recordingId)
}
