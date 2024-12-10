package com.example.myapplication.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun LineChart(
    data: List<LineChart.Line>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val allPoints = data.flatMap { it.points }
    val minX = allPoints.minOf { it.x }
    val maxX = allPoints.maxOf { it.x }
    val minY = allPoints.minOf { it.y }
    val maxY = allPoints.maxOf { it.y }
    val yRange = (maxY - minY).coerceAtLeast(0.01)

    Column(modifier = modifier) {
        // 图例
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.forEach { line ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(line.color)
                        }
                    }
                    Text(
                        text = line.label,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 图表
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 32f

            // 绘制坐标轴
            drawLine(
                color = Color.Gray,
                start = Offset(padding, height - padding),
                end = Offset(width - padding, height - padding)
            )
            drawLine(
                color = Color.Gray,
                start = Offset(padding, padding),
                end = Offset(padding, height - padding)
            )

            // 绘制网格线
            val horizontalLines = 5
            val verticalLines = data.firstOrNull()?.points?.size ?: 0
            val horizontalSpacing = (height - 2 * padding) / horizontalLines
            val verticalSpacing = (width - 2 * padding) / (verticalLines - 1)

            repeat(horizontalLines + 1) { i ->
                val y = height - padding - i * horizontalSpacing
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(padding, y),
                    end = Offset(width - padding, y)
                )
                // 绘制Y轴刻度
                val value = minY + (i * yRange / horizontalLines)
                drawContext.canvas.nativeCanvas.drawText(
                    "%.2f".format(value),
                    0f,
                    y + 4f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }

            // 绘制折线
            data.forEach { line ->
                val path = Path()
                var firstPoint = true

                line.points.forEachIndexed { index, point ->
                    val x = padding + index * verticalSpacing
                    val y = height - padding - ((point.y - minY) / yRange * (height - 2 * padding))

                    if (firstPoint) {
                        path.moveTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }

                    // 绘制数据点
                    drawCircle(
                        color = line.color,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }

                // 绘制折线
                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(width = 2f)
                )
            }

            // 绘制X轴刻度
            data.firstOrNull()?.points?.forEachIndexed { index, point ->
                val x = padding + index * verticalSpacing
                drawContext.canvas.nativeCanvas.apply {
                    rotate(-45f, x, height - padding + 8f)
                    drawText(
                        point.x,
                        x - 16f,
                        height - padding + 32f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                    rotate(45f, x, height - padding + 8f)
                }
            }
        }
    }
}

object LineChart {
    data class Line(
        val label: String,
        val color: Color,
        val points: List<Point>
    )

    data class Point(
        val x: String,
        val y: Double
    )
} 