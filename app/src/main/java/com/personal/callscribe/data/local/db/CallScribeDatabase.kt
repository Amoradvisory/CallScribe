package com.personal.callscribe.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.personal.callscribe.data.local.db.entity.DiagnosticLogEntity
import com.personal.callscribe.data.local.db.entity.RecordingEntity

/**
 * Application Room database.
 */
@Database(
    entities = [
        RecordingEntity::class,
        DiagnosticLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class CallScribeDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao
    abstract fun diagnosticLogDao(): DiagnosticLogDao
}
