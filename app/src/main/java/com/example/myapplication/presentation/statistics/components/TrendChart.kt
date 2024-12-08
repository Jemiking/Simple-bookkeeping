package com.example.myapplication.presentation.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun TrendChart(
    data: List<MonthlyAmount>,
    modifier: Modifier = Modifier
) {
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
                text = "收支趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 趋势图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 32.dp.toPx()
                    
                    // 计算数值范围
                    val maxAmount = data.maxOf { maxOf(it.income, it.expense) }
                    val minAmount = 0.0
                    val range = maxAmount - minAmount
                    
                    // 计算坐标转换
                    val xStep = (width - padding * 2) / (data.size - 1)
                    fun Double.toY() = height - padding - (this - minAmount) * (height - padding * 2) / range
                    
                    // 绘制网格线
                    val gridCount = 5
                    val gridStep = (height - padding * 2) / gridCount
                    repeat(gridCount + 1) { i ->
                        val y = padding + gridStep * i
                        drawLine(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            start = Offset(padding, y),
                            end = Offset(width - padding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // 绘制收入曲线
                    val incomePath = Path().apply {
                        data.forEachIndexed { index, amount ->
                            val x = padding + xStep * index
                            val y = amount.income.toY()
                            if (index == 0) {
                                moveTo(x, y)
                            } else {
                                lineTo(x, y)
                            }
                        }
                    }
                    drawPath(
                        path = incomePath,
                        color = MaterialTheme.colorScheme.primary,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // 绘制支出曲线
                    val expensePath = Path().apply {
                        data.forEachIndexed { index, amount ->
                            val x = padding + xStep * index
                            val y = amount.expense.toY()
                            if (index == 0) {
                                moveTo(x, y)
                            } else {
                                lineTo(x, y)
                            }
                        }
                    }
                    drawPath(
                        path = expensePath,
                        color = MaterialTheme.colorScheme.error,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // 绘制数据点
                    data.forEachIndexed { index, amount ->
                        val x = padding + xStep * index
                        
                        // 收入点
                        drawCircle(
                            color = MaterialTheme.colorScheme.primary,
                            radius = 4.dp.toPx(),
                            center = Offset(x, amount.income.toY())
                        )
                        
                        // 支出点
                        drawCircle(
                            color = MaterialTheme.colorScheme.error,
                            radius = 4.dp.toPx(),
                            center = Offset(x, amount.expense.toY())
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 收入图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 支出图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class MonthlyAmount(
    val month: YearMonth,
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense
        
    val formattedMonth: String
        get() = month.format(DateTimeFormatter.ofPattern("M月"))
} 