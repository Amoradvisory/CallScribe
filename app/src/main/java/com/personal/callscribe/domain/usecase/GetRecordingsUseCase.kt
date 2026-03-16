package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.repository.IRecordingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the recordings history.
 */
class GetRecordingsUseCase(
    private val recordingRepository: IRecordingRepository,
) {
    operator fun invoke(): Flow<List<Recording>> = recordingRepository.observeRecordings()
}
