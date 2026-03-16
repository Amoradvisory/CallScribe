package com.personal.callscribe.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personal.callscribe.data.local.db.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for recording metadata.
 */
@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :recordingId LIMIT 1")
    suspend fun getById(recordingId: Long): RecordingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: RecordingEntity): Long

    @Query("UPDATE recordings SET title = :newTitle WHERE id = :recordingId")
    suspend fun updateTitle(recordingId: Long, newTitle: String): Int

    @Query("DELETE FROM recordings WHERE id = :recordingId")
    suspend fun deleteById(recordingId: Long): Int
}
