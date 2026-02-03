package com.originalityshield.presentation.viewmodel

import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.originalityshield.domain.model.TelemetryPoint
import com.originalityshield.domain.repository.TelemetryRepository
import com.originalityshield.domain.utils.MathUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrawingViewModel(
    private val repository: TelemetryRepository
) : ViewModel() {

    private val _currentPath = MutableStateFlow<List<TelemetryPoint>>(emptyList())
    val currentPath: StateFlow<List<TelemetryPoint>> = _currentPath.asStateFlow()

    private val _pulseData = MutableStateFlow<List<Float>>(emptyList())
    val pulseData: StateFlow<List<Float>> = _pulseData.asStateFlow()

    private var lastPoint: TelemetryPoint? = null
    private var secondLastPoint: TelemetryPoint? = null

    private fun resetHistory() {
        lastPoint = null
        secondLastPoint = null
    }

    fun processInput(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            resetHistory()
        }

        val newPoints = extractPoints(event)

        if (newPoints.isEmpty()) return

        val processedPoints = mutableListOf<TelemetryPoint>()

        for (rawPoint in newPoints) {
            val processed = calculateMetrics(rawPoint)
            processedPoints.add(processed)

            secondLastPoint = lastPoint
            lastPoint = processed
        }

        _currentPath.value = _currentPath.value + processedPoints
        _pulseData.value = _pulseData.value + processedPoints.map { it.microTremor }

        viewModelScope.launch {
            repository.saveBatch(processedPoints)
        }
    }

    private fun extractPoints(event: MotionEvent): List<TelemetryPoint> {
        val points = mutableListOf<TelemetryPoint>()
        val historySize = event.historySize

        for (h in 0 until historySize) {
            points.add(TelemetryPoint(
                x = event.getHistoricalX(h),
                y = event.getHistoricalY(h),
                pressure = event.getHistoricalPressure(h),
                size = event.getHistoricalSize(h),
                timestamp = event.getHistoricalEventTime(h)
            ))
        }
        points.add(TelemetryPoint(
            x = event.x,
            y = event.y,
            pressure = event.pressure,
            size = event.size,
            timestamp = event.eventTime
        ))
        return points
    }

    private fun calculateMetrics(point: TelemetryPoint): TelemetryPoint {
        val p1 = secondLastPoint
        val p2 = lastPoint
        val p3 = point

        var velocity = 0f
        var curvature = 0f
        var microTremor = 0f

        if (p2 != null) {
            velocity = MathUtils.calculateVelocity(p2, p3)
        }

        if (p1 != null && p2 != null) {
            curvature = MathUtils.calculateCurvature(p1, p2, p3)
            // Using curvature/jitter as a proxy for micro-tremor visualization for now
            // In a full implementation, this might look at deviation from a smoothed baseline
            microTremor = curvature * velocity // Simple heuristic for now
        }

        return point.copy(
            velocity = velocity,
            curvature = curvature,
            microTremor = microTremor
        )
    }

    class Factory(private val repository: TelemetryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DrawingViewModel(repository) as T
        }
    }
}
