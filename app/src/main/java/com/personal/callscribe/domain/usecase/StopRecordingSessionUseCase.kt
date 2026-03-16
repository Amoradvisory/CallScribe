package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.phone.IRecordingSessionController

/**
 * Stops the active recording session and persists the final metadata.
 */
class StopRecordingSessionUseCase(
    private val sessionController: IRecordingSessionController,
) {
    suspend operator fun invoke(): AppResult<Recording> = sessionController.stopSession()
}
