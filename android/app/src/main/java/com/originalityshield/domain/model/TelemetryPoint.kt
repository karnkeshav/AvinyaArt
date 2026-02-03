package com.originalityshield.domain.model

data class TelemetryPoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val size: Float, // Touch Area
    val timestamp: Long,
    val velocity: Float = 0f,
    val curvature: Float = 0f,
    val microTremor: Float = 0f
)
