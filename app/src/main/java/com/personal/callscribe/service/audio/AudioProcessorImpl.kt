package com.personal.callscribe.service.audio

import com.personal.callscribe.domain.audio.IAudioProcessor

/**
 * Applies a simple gain factor to PCM16 little-endian samples.
 */
class AudioProcessorImpl : IAudioProcessor {
    override fun process(buffer: ByteArray, bytesRead: Int, gainPercent: Int): ByteArray {
        val output = buffer.copyOf(bytesRead)
        if (gainPercent == 100) {
            return output
        }
        val gain = gainPercent.coerceIn(50, 200) / 100.0
        val sampleCount = bytesRead / 2
        for (index in 0 until sampleCount) {
            val sample = ((output[index * 2 + 1].toInt() shl 8) or
                (output[index * 2].toInt() and 0xFF)).toShort().toInt()
            val boosted = (sample * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            output[index * 2] = (boosted and 0xFF).toByte()
            output[index * 2 + 1] = ((boosted shr 8) and 0xFF).toByte()
        }
        return output
    }
}
