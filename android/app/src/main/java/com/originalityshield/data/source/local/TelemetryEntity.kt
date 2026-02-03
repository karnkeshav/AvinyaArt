package com.originalityshield.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_points")
data class TelemetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val x: Float,
    val y: Float,
    val pressure: Float,
    val size: Float,
    val timestamp: Long,
    val velocity: Float,
    val curvature: Float,
    val microTremor: Float
)
