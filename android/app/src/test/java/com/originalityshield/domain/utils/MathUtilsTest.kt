package com.originalityshield.domain.utils

import com.originalityshield.domain.model.TelemetryPoint
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class MathUtilsTest {

    @Test
    fun testVelocityCalculation() {
        val p1 = TelemetryPoint(x = 0f, y = 0f, pressure = 1f, size = 1f, timestamp = 0L)
        val p2 = TelemetryPoint(x = 10f, y = 0f, pressure = 1f, size = 1f, timestamp = 1000L) // 1 second later, 10 units dist

        // dist = 10. time = 1000. velocity = 10/1000 = 0.01
        val velocity = MathUtils.calculateVelocity(p1, p2)
        assertEquals(0.01f, velocity, 0.0001f)
    }

    @Test
    fun testCurvatureCalculation_StraightLine() {
        val p1 = TelemetryPoint(x = 0f, y = 0f, pressure = 1f, size = 1f, timestamp = 0L)
        val p2 = TelemetryPoint(x = 10f, y = 0f, pressure = 1f, size = 1f, timestamp = 1000L)
        val p3 = TelemetryPoint(x = 20f, y = 0f, pressure = 1f, size = 1f, timestamp = 2000L)

        val curvature = MathUtils.calculateCurvature(p1, p2, p3)
        assertEquals(0f, curvature, 0.001f)
    }

    @Test
    fun testCurvatureCalculation_RightAngle() {
        // p1(0,10) -> p2(0,0) is vector (0, -10)
        // p2(0,0) -> p3(10,0) is vector (10, 0)
        // angle between them is 90 degrees
        val p1 = TelemetryPoint(x = 0f, y = 10f, pressure = 1f, size = 1f, timestamp = 0L)
        val p2 = TelemetryPoint(x = 0f, y = 0f, pressure = 1f, size = 1f, timestamp = 1000L)
        val p3 = TelemetryPoint(x = 10f, y = 0f, pressure = 1f, size = 1f, timestamp = 2000L)

        // MathUtils.calculateCurvature returns angle in radians
        val curvature = MathUtils.calculateCurvature(p1, p2, p3)
        assertEquals((PI / 2).toFloat(), curvature, 0.001f)
    }

    @Test
    fun testEntropyCalculation_Uniform() {
        val values = listOf(1f, 1f, 1f, 1f, 1f)
        val entropy = MathUtils.calculateEntropy(values)
        // All in same bin -> Entropy = 0
        assertEquals(0f, entropy, 0.001f)
    }
}
