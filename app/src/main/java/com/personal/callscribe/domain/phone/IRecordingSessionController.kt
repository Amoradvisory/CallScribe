package com.personal.callscribe.domain.phone

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.CalibrationResult
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.model.RecordingSession
import com.personal.callscribe.domain.model.SessionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Coordinates recording-session state transitions independently of the UI.
 */
interface IRecordingSessionController {
    val sessionState: StateFlow<SessionState>
    val callState: StateFlow<CallState>
    suspend fun startSession(): AppResult<RecordingSession>
    suspend fun stopSession(): AppResult<Recording>
    suspend fun runCalibration(durationMs: Long = 2_000L): AppResult<CalibrationResult>
    fun reset()
}
