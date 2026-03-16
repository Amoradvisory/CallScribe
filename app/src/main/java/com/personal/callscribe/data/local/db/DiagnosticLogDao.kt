package com.personal.callscribe.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personal.callscribe.data.local.db.entity.DiagnosticLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for diagnostics logs.
 */
@Dao
interface DiagnosticLogDao {
    @Query("SELECT * FROM diagnostic_logs ORDER BY timestampMillis DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<DiagnosticLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiagnosticLogEntity): Long

    @Query("DELETE FROM diagnostic_logs")
    suspend fun clear()
}
