package com.personal.callscribe.domain.model

/**
 * Runtime information for an active or finalizing recording session.
 */
data class RecordingSession(
    val sessionId: String,
    val outputFilePath: String,
    val config: AudioConfig,
    val startedAtMillis: Long,
    val speakerphoneRequested: Boolean,
    val speakerphoneActivated: Boolean,
)
