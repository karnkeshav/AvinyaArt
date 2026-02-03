package com.originalityshield.domain.utils

import com.originalityshield.domain.model.TelemetryPoint
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.log2

object MathUtils {

    fun calculateDistance(p1: TelemetryPoint, p2: TelemetryPoint): Float {
        return sqrt((p2.x - p1.x).pow(2) + (p2.y - p1.y).pow(2))
    }

    fun calculateVelocity(p1: TelemetryPoint, p2: TelemetryPoint): Float {
        val dist = calculateDistance(p1, p2)
        val timeDiff = (p2.timestamp - p1.timestamp).toFloat()
        // Avoid division by zero
        return if (timeDiff > 0) dist / timeDiff else 0f
    }

    /**
     * Calculates the curvature (angle change) between two segments formed by p1-p2 and p2-p3.
     * Returns the angle in radians.
     */
    fun calculateCurvature(p1: TelemetryPoint, p2: TelemetryPoint, p3: TelemetryPoint): Float {
        val v1x = p2.x - p1.x
        val v1y = p2.y - p1.y
        val v2x = p3.x - p2.x
        val v2y = p3.y - p2.y

        val mag1 = sqrt(v1x * v1x + v1y * v1y)
        val mag2 = sqrt(v2x * v2x + v2y * v2y)

        if (mag1 == 0f || mag2 == 0f) return 0f

        val dot = v1x * v2x + v1y * v2y
        val cosine = (dot / (mag1 * mag2)).coerceIn(-1.0f, 1.0f)
        return acos(cosine)
    }

    /**
     * Calculates the Shannon Entropy of a list of values.
     * Uses 10 bins for the histogram.
     */
    fun calculateEntropy(values: List<Float>): Float {
        if (values.isEmpty()) return 0f

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        if (min == max) return 0f

        val numBins = 10
        val binWidth = (max - min) / numBins
        val bins = IntArray(numBins)

        for (v in values) {
            val idx = ((v - min) / binWidth).toInt().coerceAtMost(numBins - 1)
            bins[idx]++
        }

        var entropy = 0.0
        val total = values.size.toFloat()
        for (count in bins) {
            if (count > 0) {
                val p = count / total
                entropy -= p * log2(p)
            }
        }
        return entropy.toFloat()
    }

    fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average().toFloat()
        val sumSqDiff = values.map { (it - mean).pow(2) }.sum()
        return sumSqDiff / values.size
    }
}
