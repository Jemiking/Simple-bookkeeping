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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*

data class PieChartData(
    val items: List<Item>
) {
    data class Item(
        val label: String,
        val value: Float,
        val color: Color
    )
}

@Composable
fun PieChart(
    data: PieChartData,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<PieChartData.Item?>(null) }
    val density = LocalDensity.current
    val strokeWidth = with(density) { 32.dp.toPx() }

    val transition = updateTransition(
        targetState = data,
        label = "PieChart"
    )

    val animatedValues = data.items.map { item ->
        val targetValue = item.value
        val animatedValue by transition.animateFloat(
            label = "value_${item.label}",
            transitionSpec = {
                tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            }
        ) { state ->
            state.items.find { it.label == item.label }?.value ?: 0f
        }
        item.copy(value = animatedValue)
    }

    Box(
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = minOf(size.width, size.height) / 2 - strokeWidth / 2
                        val touchAngle = (atan2(
                            offset.y - center.y,
                            offset.x - center.x
                        ) * 180 / PI).toFloat()
                        val normalizedAngle = if (touchAngle < 0) touchAngle + 360 else touchAngle

                        var startAngle = 0f
                        animatedValues.forEach { item ->
                            val sweepAngle = item.value * 360
                            if (normalizedAngle >= startAngle && normalizedAngle <= startAngle + sweepAngle) {
                                selectedItem = if (selectedItem == item) null else item
                                return@detectTapGestures
                            }
                            startAngle += sweepAngle
                        }
                        selectedItem = null
                    }
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = minOf(size.width, size.height) / 2 - strokeWidth / 2

            // 绘制背景圆环
            drawCircle(
                color = MaterialTheme.colorScheme.surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // 绘制数据扇形
            var startAngle = 0f
            animatedValues.forEach { item ->
                val sweepAngle = item.value * 360
                val isSelected = item == selectedItem

                // 绘制扇形
                drawArc(
                    color = item.color,
                    startAngle = startAngle - 90, // 从12点钟方向开始
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = strokeWidth * if (isSelected) 1.2f else 1f,
                        cap = StrokeCap.Round
                    )
                )

                startAngle += sweepAngle
            }
        }

        // 显示选中项的标签
        selectedItem?.let { item ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = item.color
                )
                Text(
                    text = "${(item.value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
} 