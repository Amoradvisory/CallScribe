package com.personal.callscribe.domain.audio

/**
 * Applies reversible processing to a PCM16 little-endian chunk.
 */
interface IAudioProcessor {
    fun process(buffer: ByteArray, bytesRead: Int, gainPercent: Int): ByteArray
}
