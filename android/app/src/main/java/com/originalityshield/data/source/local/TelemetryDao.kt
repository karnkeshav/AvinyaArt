package com.originalityshield.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: TelemetryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(points: List<TelemetryEntity>)

    @Query("SELECT * FROM telemetry_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getSessionPoints(sessionId: String): Flow<List<TelemetryEntity>>

    @Query("DELETE FROM telemetry_points WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
