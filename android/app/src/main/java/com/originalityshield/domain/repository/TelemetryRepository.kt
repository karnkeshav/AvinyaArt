package com.originalityshield.domain.repository

import com.originalityshield.domain.model.TelemetryPoint
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    suspend fun savePoint(point: TelemetryPoint)
    suspend fun saveBatch(points: List<TelemetryPoint>)
    fun getTelemetrySession(sessionId: String): Flow<List<TelemetryPoint>>
    suspend fun clearSession(sessionId: String)
}
