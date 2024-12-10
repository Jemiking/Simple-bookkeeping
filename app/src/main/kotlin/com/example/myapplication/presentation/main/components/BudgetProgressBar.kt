package com.example.myapplication.presentation.main.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.theme.Error
import com.example.myapplication.presentation.ui.theme.Success
import com.example.myapplication.presentation.ui.theme.Warning
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetProgressBar(
    currentAmount: Double,
    budgetAmount: Double,
    modifier: Modifier = Modifier
) {
    // 计算进度比例
    val progress = (currentAmount / budgetAmount).toFloat().coerceIn(0f, 1f)
    
    // 进度条动画
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "BudgetProgress"
    )
    
    // 根据进度确定颜色
    val progressColor = when {
        progress >= 1f -> Error
        progress >= 0.8f -> Warning
        else -> Success
    }
    
    // 格式化金额
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    val formattedCurrent = numberFormat.format(currentAmount)
    val formattedBudget = numberFormat.format(budgetAmount)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和金额
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月预算",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$formattedCurrent / $formattedBudget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 进度百分比
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = progressColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
} 