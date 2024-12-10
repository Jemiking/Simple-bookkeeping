package com.example.myapplication.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthYearPicker(
    initialYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYearMonth.year) }
    val currentYear = YearMonth.now().year
    val yearRange = (currentYear - 2..currentYear + 1).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份") },
        text = {
            Column {
                // 年份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    yearRange.forEach { year ->
                        FilterChip(
                            selected = year == selectedYear,
                            onClick = { selectedYear = year },
                            label = { Text(year.toString()) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份选择
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..12).toList()) { month ->
                        val yearMonth = YearMonth.of(selectedYear, month)
                        val isSelected = yearMonth == initialYearMonth

                        FilterChip(
                            selected = isSelected,
                            onClick = { onYearMonthSelected(yearMonth) },
                            label = {
                                Text(
                                    text = yearMonth.format(
                                        DateTimeFormatter.ofPattern("MM���")
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 