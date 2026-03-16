package com.personal.callscribe.domain.model

/**
 * Immutable audio capture configuration used across the application.
 */
data class AudioConfig(
    val sampleRateHz: Int = 16_000,
    val channelCount: Int = 1,
    val bitsPerSample: Int = 16,
    val bufferSizeInBytes: Int = 16_384,
)
