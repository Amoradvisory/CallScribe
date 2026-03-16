package com.personal.callscribe.service.audio

import com.personal.callscribe.domain.audio.IAudioLevelMonitor
import com.personal.callscribe.domain.model.AudioLevel
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Computes RMS and decibel levels from PCM16 little-endian buffers.
 */
class AudioLevelMonitorImpl : IAudioLevelMonitor {
    override fun computeLevel(buffer: ByteArray, bytesRead: Int): AudioLevel {
        if (bytesRead < 2) {
            return AudioLevel.Silent
        }
        val sampleCount = bytesRead / 2
        var sumSquares = 0.0
        var peak = 0

        for (index in 0 until sampleCount) {
            val sample = ((buffer[index * 2 + 1].toInt() shl 8) or
                (buffer[index * 2].toInt() and 0xFF)).toShort().toInt()
            val absolute = kotlin.math.abs(sample)
            if (absolute > peak) {
                peak = absolute
            }
            sumSquares += sample.toDouble() * sample.toDouble()
        }

        val rms = sqrt(sumSquares / sampleCount.toDouble())
        val normalized = max(rms / Short.MAX_VALUE.toDouble(), 1e-4)
        val decibels = 20.0 * log10(normalized)
        return AudioLevel(rms = rms, peak = peak, decibels = decibels)
    }
}
