package com.example.myapplication.presentation.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.util.formatCurrency
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.max
import kotlin.math.min

data class MonthlyComparison(
    val yearMonth: YearMonth,
    val income: Double,
    val expense: Double
)

sealed class ChartError {
    object Network : ChartError()
    object DataFormat : ChartError()
    object Unknown : ChartError()
    
    fun getMessage(): String = when (this) {
        is Network -> "网络连接失败"
        is DataFormat -> "数据格式错误"
        is Unknown -> "未知错误"
    }
    
    fun getDescription(): String = when (this) {
        is Network -> "请检查网络连接后重试"
        is DataFormat -> "请稍后重试或联系客服"
        is Unknown -> "请稍后重试"
    }
}

@Composable
fun MonthlyComparisonChart(
    data: List<MonthlyComparison>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    error: ChartError? = null,
    onRetry: () -> Unit = {}
) {
    // 缓存计算结果
    val chartData = remember(data) {
        prepareChartData(data)
    }
    
    // 缓存画笔对象
    val textPaint = remember {
        Paint().apply {
            textSize = 12.sp.toPx()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    
    // 缓存路径对象
    val incomePath = remember { Path() }
    val expensePath = remember { Path() }
    
    // 监听生命周期，清理资源
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                incomePath.reset()
                expensePath.reset()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier) {
        // 标题
        Text(
            text = "月度收支对比",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 图表区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            when {
                isLoading -> {
                    // 加载状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在加载数据...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                error != null -> {
                    // 错误状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                when (error) {
                                    is ChartError.Network -> R.drawable.ic_error_network
                                    is ChartError.DataFormat -> R.drawable.ic_error_data
                                    is ChartError.Unknown -> R.drawable.ic_error_unknown
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error.getMessage(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error.getDescription(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("重试")
                        }
                    }
                }
                data.isEmpty() -> {
                    // 空数据状态
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "暂无收支数据",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "开始记录您的第一笔交易\n体验完整的统计分析功能",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val width = size.width
                        val height = size.height
                        val padding = 48.dp.toPx()
                        val bottomPadding = 64.dp.toPx()
                        
                        // 使用缓存的数据
                        val (maxAmount, xStep, yScale) = chartData
                        
                        // 绘制网格线和金额标签
                        drawGridLinesAndLabels(
                            width = width,
                            height = height,
                            padding = padding,
                            bottomPadding = bottomPadding,
                            maxAmount = maxAmount,
                            textPaint = textPaint
                        )
                        
                        // 更新路径
                        updateChartPaths(
                            data = data,
                            incomePath = incomePath,
                            expensePath = expensePath,
                            width = width,
                            height = height,
                            padding = padding,
                            bottomPadding = bottomPadding,
                            xStep = xStep,
                            yScale = yScale
                        )
                        
                        // 绘制路径
                        drawPath(
                            path = incomePath,
                            color = MaterialTheme.colorScheme.primary,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        drawPath(
                            path = expensePath,
                            color = MaterialTheme.colorScheme.error,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        
                        // 绘制数据点和标签
                        drawDataPointsAndLabels(
                            data = data,
                            width = width,
                            height = height,
                            padding = padding,
                            bottomPadding = bottomPadding,
                            xStep = xStep,
                            yScale = yScale,
                            textPaint = textPaint
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 图例
        if (data.isNotEmpty() && error == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 收入图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(top = 4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 支出图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(top = 4.dp)
                            .background(MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun prepareChartData(data: List<MonthlyComparison>): Triple<Double, Float, Float> {
    if (data.isEmpty()) return Triple(0.0, 0f, 0f)
    
    val maxAmount = data.maxOf { max(it.income, it.expense) }
    val xStep = 1f / (data.size - 1)
    val yScale = 1f / maxAmount.toFloat()
    
    return Triple(maxAmount, xStep, yScale)
}

private fun DrawScope.drawGridLinesAndLabels(
    width: Float,
    height: Float,
    padding: Float,
    bottomPadding: Float,
    maxAmount: Double,
    textPaint: Paint
) {
    val gridCount = 5
    val gridStep = (height - padding - bottomPadding) / gridCount
    
    repeat(gridCount + 1) { i ->
        val y = padding + gridStep * i
        val amount = maxAmount - (maxAmount * i / gridCount)
        
        // 绘制网格线
        drawLine(
            color = MaterialTheme.colorScheme.surfaceVariant,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1.dp.toPx()
        )
        
        // 绘制金额标签
        drawIntoCanvas { canvas ->
            textPaint.color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
            canvas.nativeCanvas.drawText(
                amount.formatCurrency(),
                padding - 8.dp.toPx(),
                y + textPaint.textSize / 3,
                textPaint
            )
        }
    }
}

private fun updateChartPaths(
    data: List<MonthlyComparison>,
    incomePath: Path,
    expensePath: Path,
    width: Float,
    height: Float,
    padding: Float,
    bottomPadding: Float,
    xStep: Float,
    yScale: Float
) {
    incomePath.reset()
    expensePath.reset()
    
    data.forEachIndexed { index, monthData ->
        val x = padding + (width - padding * 2) * index * xStep
        val incomeY = height - bottomPadding - monthData.income.toFloat() * yScale * (height - padding - bottomPadding)
        val expenseY = height - bottomPadding - monthData.expense.toFloat() * yScale * (height - padding - bottomPadding)
        
        if (index == 0) {
            incomePath.moveTo(x, incomeY)
            expensePath.moveTo(x, expenseY)
        } else {
            incomePath.lineTo(x, incomeY)
            expensePath.lineTo(x, expenseY)
        }
    }
}

private fun DrawScope.drawDataPointsAndLabels(
    data: List<MonthlyComparison>,
    width: Float,
    height: Float,
    padding: Float,
    bottomPadding: Float,
    xStep: Float,
    yScale: Float,
    textPaint: Paint
) {
    val formatter = DateTimeFormatter.ofPattern("MM月")
    
    data.forEachIndexed { index, monthData ->
        val x = padding + (width - padding * 2) * index * xStep
        val incomeY = height - bottomPadding - monthData.income.toFloat() * yScale * (height - padding - bottomPadding)
        val expenseY = height - bottomPadding - monthData.expense.toFloat() * yScale * (height - padding - bottomPadding)
        
        // 绘制收入点和标签
        drawCircle(
            color = MaterialTheme.colorScheme.primary,
            radius = 4.dp.toPx(),
            center = Offset(x, incomeY)
        )
        
        drawIntoCanvas { canvas ->
            textPaint.color = MaterialTheme.colorScheme.primary.toArgb()
            canvas.nativeCanvas.drawText(
                monthData.income.formatCurrency(),
                x,
                incomeY - 8.dp.toPx(),
                textPaint
            )
        }
        
        // 绘制支出点和标签
        drawCircle(
            color = MaterialTheme.colorScheme.error,
            radius = 4.dp.toPx(),
            center = Offset(x, expenseY)
        )
        
        drawIntoCanvas { canvas ->
            textPaint.color = MaterialTheme.colorScheme.error.toArgb()
            canvas.nativeCanvas.drawText(
                monthData.expense.formatCurrency(),
                x,
                expenseY + 16.dp.toPx(),
                textPaint
            )
        }
        
        // 绘制月份标签
        drawIntoCanvas { canvas ->
            textPaint.color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
            canvas.nativeCanvas.drawText(
                monthData.yearMonth.format(formatter),
                x,
                height - 16.dp.toPx(),
                textPaint
            )
        }
    }
} 