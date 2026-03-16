package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.repository.IRecordingRepository

/**
 * Loads one recording by identifier.
 */
class GetRecordingDetailsUseCase(
    private val recordingRepository: IRecordingRepository,
) {
    suspend operator fun invoke(recordingId: Long): AppResult<Recording> =
        recordingRepository.getRecording(recordingId)
}
