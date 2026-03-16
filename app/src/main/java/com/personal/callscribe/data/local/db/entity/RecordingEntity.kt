package com.personal.callscribe.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing persisted recording metadata.
 */
@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val fileName: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAtMillis: Long,
    val sampleRateHz: Int,
    val channelCount: Int,
    val speakerphoneRequested: Boolean,
    val speakerphoneActivated: Boolean,
)
