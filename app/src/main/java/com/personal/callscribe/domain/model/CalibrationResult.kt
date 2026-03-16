package com.personal.callscribe.domain.model

/**
 * Result of the microphone environment calibration flow.
 */
data class CalibrationResult(
    val averageDecibels: Double,
    val recommendedGainPercent: Int,
    val recommendedSpeakerVolumePercent: Int,
    val recommendation: String,
)
