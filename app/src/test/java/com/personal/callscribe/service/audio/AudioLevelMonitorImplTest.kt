package com.personal.callscribe.service.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for audio level computation.
 */
class AudioLevelMonitorImplTest {
    private val monitor = AudioLevelMonitorImpl()

    @Test
    fun `computeLevel returns silent for short buffers`() {
        val level = monitor.computeLevel(byteArrayOf(0x00), 1)

        assertEquals(0.0, level.rms, 0.0)
        assertEquals(0, level.peak)
        assertTrue(level.decibels <= -90.0)
    }

    @Test
    fun `computeLevel returns expected peak and positive rms`() {
        val buffer = byteArrayOf(
            0x00, 0x40, // 16384
            0x00, 0x20, // 8192
        )

        val level = monitor.computeLevel(buffer, buffer.size)

        assertEquals(16384, level.peak)
        assertTrue(level.rms > 0.0)
        assertTrue(level.decibels < 0.0)
    }
}
