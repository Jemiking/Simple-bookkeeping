package com.example.myapplication.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.MonthlyStatistics
import java.time.format.DateTimeFormatter

@Composable
fun YearlyStatisticsScreen(
    viewModel: YearlyStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showYearPicker by remember { mutableStateOf(false) }

    if (showYearPicker) {
        YearPickerDialog(
            initialYear = state.selectedYear,
            onYearSelected = { year ->
                viewModel.onEvent(YearlyStatisticsEvent.SelectYear(year))
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 年份选择器
        TextButton(
            onClick = { showYearPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${state.selectedYear}年",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 年度总览
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "总收入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "¥%.2f".format(state.totalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "总支出",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "¥%.2f".format(state.totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Column {
                        Text(
                            text = "总结余",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "¥%.2f".format(state.balance),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.balance >= 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "月均收入",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "¥%.2f".format(state.averageIncome),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "月均支出",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "¥%.2f".format(state.averageExpense),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 月度统计列表
        if (state.monthlyStatistics.isNotEmpty()) {
            Text(
                text = "月度统计",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.monthlyStatistics) { monthly ->
                    MonthlyStatisticsItem(monthly = monthly)
                }
            }
        }
    }
}

@Composable
fun MonthlyStatisticsItem(
    monthly: MonthlyStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = monthly.yearMonth.format(
                    DateTimeFormatter.ofPattern("yyyy年MM月")
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(monthly.totalIncome),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(monthly.totalExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column {
                    Text(
                        text = "结余",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(monthly.balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (monthly.balance >= 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun YearPickerDialog(
    initialYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear = java.time.Year.now().value
    val yearRange = (currentYear - 2..currentYear + 1).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年份") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                yearRange.forEach { year ->
                    FilterChip(
                        selected = year == initialYear,
                        onClick = { onYearSelected(year) },
                        label = { Text(year.toString()) }
                    )
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