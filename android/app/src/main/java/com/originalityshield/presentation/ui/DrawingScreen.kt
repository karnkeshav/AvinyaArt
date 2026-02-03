package com.originalityshield.presentation.ui

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import com.originalityshield.presentation.viewmodel.DrawingViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingScreen(viewModel: DrawingViewModel) {
    val pathPoints by viewModel.currentPath.collectAsState()
    val pulseData by viewModel.pulseData.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE,
                        MotionEvent.ACTION_UP -> {
                            viewModel.processInput(event)
                            true
                        }
                        else -> false
                    }
                }
        ) {
            if (pathPoints.size > 1) {
                val path = Path()
                path.moveTo(pathPoints.first().x, pathPoints.first().y)
                for (i in 1 until pathPoints.size) {
                    val p = pathPoints[i]
                    path.lineTo(p.x, p.y)
                }

                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        PulseGraph(
            data = pulseData,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(200.dp, 100.dp)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun PulseGraph(data: List<Float>, modifier: Modifier = Modifier) {
    val windowSize = 100
    val recentData = data.takeLast(windowSize)

    Canvas(modifier = modifier) {
        if (recentData.isEmpty()) return@Canvas

        val widthPerPoint = size.width / windowSize
        val maxVal = (recentData.maxOrNull() ?: 1f).coerceAtLeast(0.1f)

        val path = Path()

        recentData.forEachIndexed { index, value ->
            val x = index * widthPerPoint
            val y = size.height - (value / maxVal) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color.Red,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
