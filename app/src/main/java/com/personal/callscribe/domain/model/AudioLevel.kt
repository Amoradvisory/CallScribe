package com.personal.callscribe.domain.model

/**
 * Describes a measured audio level from a PCM16 audio chunk.
 */
data class AudioLevel(
    val rms: Double,
    val peak: Int,
    val decibels: Double,
) {
    companion object {
        val Silent = AudioLevel(
            rms = 0.0,
            peak = 0,
            decibels = -90.0,
        )
    }
}
