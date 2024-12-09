package com.example.myapplication.presentation.components.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class LineChartData(
    val lines: List<Line>
) {
    data class Line(
        val label: String,
        val points: List<Point>,
        val color: Color
    )

    data class Point(
        val x: Float,
        val y: Float,
        val label: String
    )
}

@Composable
fun LineChart(
    data: LineChartData,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<LineChartData.Point?>(null) }
    val density = LocalDensity.current
    val strokeWidth = with(density) { 2.dp.toPx() }
    val pointRadius = with(density) { 4.dp.toPx() }
    val selectedPointRadius = with(density) { 6.dp.toPx() }
    val labelSpacing = with(density) { 4.dp.toPx() }

    val transition = updateTransition(
        targetState = data,
        label = "LineChart"
    )

    val animatedPoints = data.lines.map { line ->
        line.points.map { point ->
            val animatedY by transition.animateFloat(
                label = "point_y_${point.x}",
                transitionSpec = {
                    tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                }
            ) { state ->
                state.lines.find { it.label == line.label }
                    ?.points?.find { it.x == point.x }
                    ?.y ?: 0f
            }
            point.copy(y = animatedY)
        }
    }

    Box(
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val xStep = chartWidth / (data.lines.firstOrNull()?.points?.size?.minus(1) ?: 1)
                        val yStep = chartHeight

                        data.lines.forEach { line ->
                            line.points.forEach { point ->
                                val x = point.x * xStep
                                val y = chartHeight - (point.y * yStep)
                                val distance = kotlin.math.sqrt(
                                    (offset.x - x).pow(2) + (offset.y - y).pow(2)
                                )
                                if (distance <= pointRadius * 2) {
                                    selectedPoint = point
                                    return@detectTapGestures
                                }
                            }
                        }
                        selectedPoint = null
                    }
                }
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val xStep = chartWidth / (data.lines.firstOrNull()?.points?.size?.minus(1) ?: 1)
            val yStep = chartHeight

            // 绘制网格线
            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            val gridCount = 5
            for (i in 0..gridCount) {
                val y = chartHeight * (i.toFloat() / gridCount)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }

            // 绘制折线和点
            data.lines.forEachIndexed { lineIndex, line ->
                val path = Path()
                val points = animatedPoints[lineIndex]

                points.forEachIndexed { index, point ->
                    val x = point.x * xStep
                    val y = chartHeight - (point.y * yStep)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // 绘制点
                    val isSelected = point == selectedPoint
                    drawCircle(
                        color = line.color,
                        radius = if (isSelected) selectedPointRadius else pointRadius,
                        center = Offset(x, y)
                    )
                }

                // 绘制折线
                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 绘制渐变区域
                val fillPath = Path()
                fillPath.addPath(path)
                fillPath.lineTo(chartWidth, chartHeight)
                fillPath.lineTo(0f, chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            line.color.copy(alpha = 0.2f),
                            line.color.copy(alpha = 0.0f)
                        )
                    )
                )
            }
        }

        // 显示选中点的标签
        selectedPoint?.let { point ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                data.lines.forEach { line ->
                    val value = line.points.find { it.x == point.x }?.y ?: 0f
                    Text(
                        text = "${line.label}: ${(value * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = line.color
                    )
                }
            }
        }
    }
} 