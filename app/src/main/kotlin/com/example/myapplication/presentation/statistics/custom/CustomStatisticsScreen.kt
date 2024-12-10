package com.example.myapplication.presentation.statistics.custom

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.domain.model.StatisticsDimension
import com.example.myapplication.presentation.components.chart.LineChart
import com.example.myapplication.presentation.components.chart.LineChartData
import com.example.myapplication.presentation.components.chart.PieChart
import com.example.myapplication.presentation.components.chart.PieChartData
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomStatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CustomStatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自定义统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 维度选择
                    TextButton(onClick = { viewModel.toggleDimensionDropdown() }) {
                        Text(state.selectedDimension.toString())
                        Icon(
                            imageVector = if (state.showDimensionDropdown) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = state.showDimensionDropdown,
                        onDismissRequest = { viewModel.toggleDimensionDropdown() }
                    ) {
                        state.availableDimensions.forEach { dimension ->
                            DropdownMenuItem(
                                text = { Text(dimension.toString()) },
                                onClick = {
                                    viewModel.selectDimension(dimension)
                                    viewModel.toggleDimensionDropdown()
                                }
                            )
                        }
                    }

                    // 时间范围选择
                    TextButton(onClick = { viewModel.toggleRangeDropdown() }) {
                        Text(state.selectedRange.toString())
                        Icon(
                            imageVector = if (state.showRangeDropdown) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = state.showRangeDropdown,
                        onDismissRequest = { viewModel.toggleRangeDropdown() }
                    ) {
                        state.availableRanges.forEach { range ->
                            DropdownMenuItem(
                                text = { Text(range.toString()) },
                                onClick = {
                                    viewModel.selectRange(range)
                                    viewModel.toggleRangeDropdown()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 总览卡片
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "总览",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "总收入",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = numberFormat.format(state.totalIncome),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "总支出",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = numberFormat.format(state.totalExpense),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 趋势图
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "趋势",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LineChart(
                                data = LineChartData(
                                    lines = listOf(
                                        LineChartData.Line(
                                            label = "收入",
                                            points = state.incomeData,
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        LineChartData.Line(
                                            label = "支出",
                                            points = state.expenseData,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }

                    // 分布图
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "分布",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = { viewModel.toggleIncomeVisible() },
                                        label = { Text("收入") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (state.showIncome) {
                                                    Icons.Default.CheckCircle
                                                } else {
                                                    Icons.Default.RadioButtonUnchecked
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                    AssistChip(
                                        onClick = { viewModel.toggleExpenseVisible() },
                                        label = { Text("支出") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (state.showExpense) {
                                                    Icons.Default.CheckCircle
                                                } else {
                                                    Icons.Default.RadioButtonUnchecked
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }

                            if (state.showIncome) {
                                PieChart(
                                    data = PieChartData(
                                        items = state.incomeDistributionData
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                state.incomeDistributionData.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(item.color, CircleShape)
                                            )
                                            Text(item.label)
                                        }
                                        Text(
                                            text = "${(item.value * 100).toInt()}%",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (state.showExpense) {
                                PieChart(
                                    data = PieChartData(
                                        items = state.expenseDistributionData
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                state.expenseDistributionData.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(item.color, CircleShape)
                                            )
                                            Text(item.label)
                                        }
                                        Text(
                                            text = "${(item.value * 100).toInt()}%",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 错误提示
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("确定")
                        }
                    }
                ) {
                    Text(state.error)
                }
            }
        }
    }
} 