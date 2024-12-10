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
import com.example.myapplication.domain.model.CategoryTrend
import com.example.myapplication.presentation.components.LineChart
import java.time.format.DateTimeFormatter

@Composable
fun CategoryTrendScreen(
    viewModel: CategoryTrendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    if (showDateRangePicker) {
        DateRangePickerDialog(
            initialStartMonth = state.startMonth,
            initialEndMonth = state.endMonth,
            onDateRangeSelected = { start, end ->
                viewModel.onEvent(CategoryTrendEvent.SelectDateRange(start, end))
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 日期范围选择器
        TextButton(
            onClick = { showDateRangePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${state.startMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} - " +
                        state.endMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 分类选择
        if (state.categories.isNotEmpty()) {
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories) { category ->
                    FilterChip(
                        selected = state.selectedCategoryIds.contains(category.id),
                        onClick = {
                            viewModel.onEvent(CategoryTrendEvent.ToggleCategory(category.id))
                        },
                        label = { Text(category.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 趋势图
        if (state.trends.isNotEmpty()) {
            Text(
                text = "趋势分析",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LineChart(
                data = state.trends.map { trend ->
                    LineChart.Line(
                        label = trend.categoryName,
                        color = Color(android.graphics.Color.parseColor(trend.categoryColor)),
                        points = trend.monthlyAmounts.map { amount ->
                            LineChart.Point(
                                x = amount.yearMonth.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM")
                                ),
                                y = amount.amount
                            )
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            // 趋势数据列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.trends) { trend ->
                    CategoryTrendItem(trend = trend)
                }
            }
        }
    }
}

@Composable
fun CategoryTrendItem(
    trend: CategoryTrend,
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
                text = trend.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = Color(android.graphics.Color.parseColor(trend.categoryColor))
            )

            Spacer(modifier = Modifier.height(8.dp))

            trend.monthlyAmounts.forEach { amount ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = amount.yearMonth.format(
                            DateTimeFormatter.ofPattern("yyyy年MM月")
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥%.2f".format(amount.amount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DateRangePickerDialog(
    initialStartMonth: java.time.YearMonth,
    initialEndMonth: java.time.YearMonth,
    onDateRangeSelected: (java.time.YearMonth, java.time.YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var startMonth by remember { mutableStateOf(initialStartMonth) }
    var endMonth by remember { mutableStateOf(initialEndMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期范围") },
        text = {
            Column {
                Text(
                    text = "开始月份",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MonthYearPicker(
                    initialYearMonth = startMonth,
                    onYearMonthSelected = { startMonth = it },
                    onDismiss = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "结束月份",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MonthYearPicker(
                    initialYearMonth = endMonth,
                    onYearMonthSelected = { endMonth = it },
                    onDismiss = {}
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startMonth <= endMonth) {
                        onDateRangeSelected(startMonth, endMonth)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 