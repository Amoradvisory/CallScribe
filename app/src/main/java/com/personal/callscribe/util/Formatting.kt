package com.personal.callscribe.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRANCE)
    .withZone(ZoneId.systemDefault())
private val decimalFormatter = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US))

/**
 * Formats milliseconds into a compact duration string.
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

/**
 * Formats bytes into a readable file size.
 */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024L) {
        return "$bytes B"
    }
    val kb = bytes / 1024.0
    if (kb < 1024.0) {
        return "${decimalFormatter.format(kb)} KB"
    }
    val mb = kb / 1024.0
    return "${decimalFormatter.format(mb)} MB"
}

/**
 * Formats epoch milliseconds into local date and time.
 */
fun formatDateTime(epochMillis: Long): String = dateFormatter.format(Instant.ofEpochMilli(epochMillis))
