package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.RecordingSession
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.domain.phone.IRecordingSessionController

/**
 * Starts a new recording session from the idle state.
 */
class StartRecordingSessionUseCase(
    private val sessionController: IRecordingSessionController,
) {
    suspend operator fun invoke(): AppResult<RecordingSession> {
        when (sessionController.sessionState.value) {
            is SessionState.Completed,
            is SessionState.Error,
            SessionState.Idle,
            is SessionState.Preparing,
            is SessionState.EnablingSpeaker,
            is SessionState.Finalizing,
            is SessionState.Recording,
            -> Unit
        }
        if (sessionController.sessionState.value is SessionState.Completed ||
            sessionController.sessionState.value is SessionState.Error
        ) {
            sessionController.reset()
        }
        return sessionController.startSession()
    }
}
