package com.personal.callscribe.domain.audio

import com.personal.callscribe.domain.model.AudioLevel

/**
 * Computes metrics from PCM audio buffers.
 */
interface IAudioLevelMonitor {
    fun computeLevel(buffer: ByteArray, bytesRead: Int): AudioLevel
}
