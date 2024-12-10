package com.example.myapplication.presentation.statistics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.usecase.transaction.GetTransactionStatsUseCase.CategoryStat
import kotlin.math.*

@Composable
fun PieChart(
    stats: List<CategoryStat>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var center by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(Size.Zero) }
    val radius by remember { mutableStateOf(0f) }
    
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "分类占比",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 饼图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            // 计算点击位置与圆心的角度
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)
                            
                            // 如果点击在圆内
                            if (distance <= radius) {
                                val angle = (atan2(dy, dx) * 180 / PI).toFloat()
                                val normalizedAngle = if (angle < 0) angle + 360 else angle
                                
                                // 找到对应的分类
                                var currentAngle = 0f
                                stats.forEach { stat ->
                                    val sweepAngle = (stat.percentage * 360 / 100).toFloat()
                                    if (normalizedAngle >= currentAngle && 
                                        normalizedAngle <= currentAngle + sweepAngle) {
                                        onCategorySelected(
                                            if (selectedCategoryId == stat.categoryId) null 
                                            else stat.categoryId
                                        )
                                        return@detectTapGestures
                                    }
                                    currentAngle += sweepAngle
                                }
                            } else {
                                onCategorySelected(null)
                            }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    size = this.size
                    center = Offset(size.width / 2, size.height / 2)
                    radius = minOf(size.width, size.height) / 2 * 0.8f
                    
                    var startAngle = 0f
                    stats.forEach { stat ->
                        val sweepAngle = (stat.percentage * 360 / 100).toFloat()
                        val isSelected = selectedCategoryId == stat.categoryId
                        
                        // 绘制扇形
                        drawArc(
                            color = Color(stat.categoryColor),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(
                                center.x - radius,
                                center.y - radius
                            ),
                            alpha = if (isSelected) 1f else 0.6f
                        )
                        
                        // 绘制边框
                        if (isSelected) {
                            drawArc(
                                color = MaterialTheme.colorScheme.surface,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(radius * 2.1f, radius * 2.1f),
                                topLeft = Offset(
                                    center.x - radius * 1.05f,
                                    center.y - radius * 1.05f
                                ),
                                style = Stroke(width = 4.dp.toPx())
                            )
                        }
                        
                        startAngle += sweepAngle
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 图例
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stats.forEach { stat ->
                    val isSelected = selectedCategoryId == stat.categoryId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 分类名称和图标
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color(stat.categoryColor).copy(
                                            alpha = if (isSelected) 1f else 0.6f
                                        ),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(
                                text = stat.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        // 百分比
                        Text(
                            text = "%.1f%%".format(stat.percentage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
} 