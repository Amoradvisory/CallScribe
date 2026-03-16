package com.personal.callscribe.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for formatting helpers.
 */
class FormattingTest {
    @Test
    fun `formatDuration renders minutes and seconds`() {
        assertEquals("02:05", formatDuration(125_000L))
    }

    @Test
    fun `formatDuration renders hours when needed`() {
        assertEquals("01:02:03", formatDuration(3_723_000L))
    }

    @Test
    fun `formatBytes renders kilobytes and megabytes`() {
        assertEquals("1.0 KB", formatBytes(1024L))
        assertEquals("2.0 MB", formatBytes(2L * 1024L * 1024L))
    }
}
