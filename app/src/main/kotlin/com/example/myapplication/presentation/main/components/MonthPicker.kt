package com.example.myapplication.presentation.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthPicker(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 上个月按钮
        IconButton(
            onClick = {
                onMonthSelected(selectedMonth.minusMonths(1))
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上个月"
            )
        }

        // 当前月份
        Text(
            text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
            style = MaterialTheme.typography.titleMedium
        )

        // 下个月按钮
        IconButton(
            onClick = {
                onMonthSelected(selectedMonth.plusMonths(1))
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下个月"
            )
        }
    }
} 