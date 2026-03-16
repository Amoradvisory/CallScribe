package com.personal.callscribe.domain.model

/**
 * Persisted recording metadata displayed in history and details screens.
 */
data class Recording(
    val id: Long = 0L,
    val title: String,
    val fileName: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAtMillis: Long,
    val sampleRateHz: Int,
    val channelCount: Int,
    val speakerphoneRequested: Boolean,
    val speakerphoneActivated: Boolean,
)
