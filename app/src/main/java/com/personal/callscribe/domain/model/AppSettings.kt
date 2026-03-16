package com.personal.callscribe.domain.model

/**
 * Persisted user-configurable settings.
 */
data class AppSettings(
    val speakerphoneEnabled: Boolean = true,
    val autoStartOnCall: Boolean = false,
    val keepScreenOn: Boolean = true,
    val preferredSampleRateHz: Int = 16_000,
    val inputGainPercent: Int = 100,
    val enableDiagnostics: Boolean = true,
    val preferredFilePrefix: String = "CallScribe",
)
