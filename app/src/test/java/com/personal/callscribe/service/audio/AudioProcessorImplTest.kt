package com.personal.callscribe.service.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test

/**
 * Unit tests for PCM gain processing.
 */
class AudioProcessorImplTest {
    private val processor = AudioProcessorImpl()

    @Test
    fun `process returns identical bytes when gain is 100 percent`() {
        val input = byteArrayOf(0x10, 0x00, 0x20, 0x00)

        val output = processor.process(input, input.size, 100)

        assertArrayEquals(input, output)
    }

    @Test
    fun `process amplifies pcm16 samples with saturation`() {
        val input = byteArrayOf(
            0x00, 0x40, // 16384
            0x00, 0x7F, // 32512
        )

        val output = processor.process(input, input.size, 200)

        assertArrayEquals(
            byteArrayOf(
                0xFF.toByte(), 0x7F, // 32768 clamped to 32767
                0xFF.toByte(), 0x7F, // clamped to 32767
            ),
            output,
        )
    }
}
