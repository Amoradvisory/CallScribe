package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.CalibrationResult
import com.personal.callscribe.domain.phone.IRecordingSessionController

/**
 * Runs a short microphone capture to suggest gain and speaker adjustments.
 */
class RunCalibrationUseCase(
    private val sessionController: IRecordingSessionController,
) {
    suspend operator fun invoke(durationMs: Long = 2_000L): AppResult<CalibrationResult> =
        sessionController.runCalibration(durationMs)
}
