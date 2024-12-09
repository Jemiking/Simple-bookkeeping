package com.example.myapplication.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.CategoryStatistics
import com.example.myapplication.domain.model.DailyStatistics
import com.example.myapplication.presentation.components.MonthYearPicker
import com.example.myapplication.presentation.components.PieChart
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MonthlyStatisticsScreen(
    viewModel: MonthlyStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthYearPicker(
            initialYearMonth = state.selectedMonth,
            onYearMonthSelected = { yearMonth ->
                viewModel.onEvent(MonthlyStatisticsEvent.SelectMonth(yearMonth))
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 月份选择器
        TextButton(
            onClick = { showMonthPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.selectedMonth.format(
                    DateTimeFormatter.ofPattern("yyyy年MM月")
                ),
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 总览卡片
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
                            text = "收入",
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
                            text = "支出",
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
                            text = "结余",
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
            }
        }

        // 分类统计
        if (state.categoryStatistics.isNotEmpty()) {
            Text(
                text = "分类统计",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 饼图
            PieChart(
                data = state.categoryStatistics.map { category ->
                    PieChart.Slice(
                        value = category.amount,
                        color = Color(android.graphics.Color.parseColor(category.categoryColor))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            // 分类列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categoryStatistics) { category ->
                    CategoryStatisticsItem(category = category)
                }
            }
        }

        // 每日统计
        if (state.dailyStatistics.isNotEmpty()) {
            Text(
                text = "每日统计",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.dailyStatistics) { daily ->
                    DailyStatisticsItem(daily = daily)
                }
            }
        }
    }
}

@Composable
fun CategoryStatisticsItem(
    category: CategoryStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${category.count}笔",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "¥%.2f".format(category.amount),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${category.percentage.roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        LinearProgressIndicator(
            progress = (category.percentage / 100).toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color(android.graphics.Color.parseColor(category.categoryColor))
        )
    }
}

@Composable
fun DailyStatisticsItem(
    daily: DailyStatistics,
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
                text = daily.date,
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
                        text = "¥%.2f".format(daily.income),
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
                        text = "¥%.2f".format(daily.expense),
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
                        text = "¥%.2f".format(daily.balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (daily.balance >= 0) {
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