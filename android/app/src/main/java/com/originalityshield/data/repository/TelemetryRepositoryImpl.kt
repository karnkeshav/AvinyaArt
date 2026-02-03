package com.originalityshield.data.repository

import com.originalityshield.data.source.local.TelemetryDao
import com.originalityshield.data.source.local.TelemetryEntity
import com.originalityshield.domain.model.TelemetryPoint
import com.originalityshield.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TelemetryRepositoryImpl(
    private val telemetryDao: TelemetryDao
) : TelemetryRepository {

    override suspend fun savePoint(point: TelemetryPoint) {
        telemetryDao.insertPoint(point.toEntity("current_session"))
    }

    override suspend fun saveBatch(points: List<TelemetryPoint>) {
         telemetryDao.insertBatch(points.map { it.toEntity("current_session") })
    }

    override fun getTelemetrySession(sessionId: String): Flow<List<TelemetryPoint>> {
        return telemetryDao.getSessionPoints(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun clearSession(sessionId: String) {
        telemetryDao.deleteSession(sessionId)
    }

    private fun TelemetryPoint.toEntity(sessionId: String): TelemetryEntity {
        return TelemetryEntity(
            sessionId = sessionId,
            x = x,
            y = y,
            pressure = pressure,
            size = size,
            timestamp = timestamp,
            velocity = velocity,
            curvature = curvature,
            microTremor = microTremor
        )
    }

    private fun TelemetryEntity.toDomain(): TelemetryPoint {
        return TelemetryPoint(
            x = x,
            y = y,
            pressure = pressure,
            size = size,
            timestamp = timestamp,
            velocity = velocity,
            curvature = curvature,
            microTremor = microTremor
        )
    }
}
