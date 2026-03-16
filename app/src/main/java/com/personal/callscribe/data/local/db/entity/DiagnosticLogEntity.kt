package com.personal.callscribe.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for structured diagnostics.
 */
@Entity(tableName = "diagnostic_logs")
data class DiagnosticLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long,
    val level: String,
    val tag: String,
    val message: String,
    val details: String? = null,
)
