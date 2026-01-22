package com.example.ddcp.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ddcp.network.RetrofitClient
import com.example.ddcp.network.TaskSubmission
import com.example.ddcp.network.TelemetryData
import kotlinx.coroutines.launch

data class DrawPoint(val x: Float, val y: Float, val pressure: Float)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreativeCanvas(navController: NavController, taskId: Int) {
    val paths = remember { mutableStateListOf<Pair<Path, Float>>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPressure by remember { mutableStateOf(1.0f) }

    // Telemetry capture
    val pressureSamples = remember { mutableListOf<Float>() }
    val velocitySamples = remember { mutableListOf<Float>() } // Simplified for now
    val jitterSamples = remember { mutableListOf<Float>() }
    val timestamps = remember { mutableListOf<Long>() }

    val scope = rememberCoroutineScope()
    var submissionStatus by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            currentPath = Path().apply { moveTo(event.x, event.y) }
                            currentPressure = event.pressure
                            pressureSamples.add(event.pressure)
                            timestamps.add(System.currentTimeMillis())
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            currentPath?.lineTo(event.x, event.y)
                            currentPressure = event.pressure
                            pressureSamples.add(event.pressure)
                            timestamps.add(System.currentTimeMillis())
                            // Redraw
                            paths.add(currentPath!! to currentPressure)
                            currentPath = Path().apply { moveTo(event.x, event.y) }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            currentPath?.lineTo(event.x, event.y)
                            paths.add(currentPath!! to currentPressure)
                            currentPath = null
                            timestamps.add(System.currentTimeMillis())
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { (path, pressure) ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 5f * pressure)
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                scope.launch {
                    try {
                        submissionStatus = "Submitting..."
                        val telemetry = TelemetryData(
                            user_id = 1, // Mocked, ideally from session
                            pressure_samples = pressureSamples.toList(),
                            velocity_samples = velocitySamples.toList(),
                            jitter_samples = jitterSamples.toList(),
                            stroke_timestamps = timestamps.toList()
                        )

                        val response = RetrofitClient.api.submitTask(
                            taskId,
                            TaskSubmission(submission_url = "http://mock-url.com/img.png", telemetry_data = telemetry)
                        )

                        submissionStatus = "Submitted! Human Score: ${response.human_confidence}"
                        // Optionally navigate back
                    } catch (e: Exception) {
                        submissionStatus = "Error: ${e.message}"
                    }
                }
            }) {
                Text("Submit Artwork")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(submissionStatus)
        }
    }
}
