package com.personal.callscribe.domain.model

import com.personal.callscribe.domain.error.AppError

/**
 * Formal state machine for the recording lifecycle.
 */
sealed class SessionState {
    data object Idle : SessionState()
    data class Preparing(val message: String) : SessionState()
    data class EnablingSpeaker(val session: RecordingSession) : SessionState()
    data class Recording(
        val session: RecordingSession,
        val audioLevel: AudioLevel,
        val elapsedMs: Long,
    ) : SessionState()

    data class Finalizing(val session: RecordingSession) : SessionState()
    data class Completed(val recording: com.personal.callscribe.domain.model.Recording) : SessionState()
    data class Error(val error: AppError) : SessionState()
}
