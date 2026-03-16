package com.personal.callscribe.domain.model

/**
 * A persisted log entry used for diagnostics in the UI.
 */
data class DiagnosticLog(
    val id: Long,
    val timestampMillis: Long,
    val level: DiagnosticLevel,
    val tag: String,
    val message: String,
    val details: String? = null,
)
