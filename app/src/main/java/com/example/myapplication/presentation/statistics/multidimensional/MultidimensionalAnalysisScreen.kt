package com.example.myapplication.presentation.statistics.multidimensional

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
fun MultidimensionalAnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: MultidimensionalAnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("多维度分析") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 主维度选择
                    TextButton(onClick = { viewModel.togglePrimaryDimensionDropdown() }) {
                        Text(state.selectedPrimaryDimension.toString())
                        Icon(
                            imageVector = if (state.showPrimaryDimensionDropdown) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = state.showPrimaryDimensionDropdown,
                        onDismissRequest = { viewModel.togglePrimaryDimensionDropdown() }
                    ) {
                        state.availableDimensions.forEach { dimension ->
                            DropdownMenuItem(
                                text = { Text(dimension.toString()) },
                                onClick = {
                                    viewModel.selectPrimaryDimension(dimension)
                                    viewModel.togglePrimaryDimensionDropdown()
                                }
                            )
                        }
                    }

                    // 次维度选择
                    TextButton(onClick = { viewModel.toggleSecondaryDimensionDropdown() }) {
                        Text(state.selectedSecondaryDimension.toString())
                        Icon(
                            imageVector = if (state.showSecondaryDimensionDropdown) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                    DropdownMenu(
                        expanded = state.showSecondaryDimensionDropdown,
                        onDismissRequest = { viewModel.toggleSecondaryDimensionDropdown() }
                    ) {
                        state.availableDimensions
                            .filter { it != state.selectedPrimaryDimension }
                            .forEach { dimension ->
                                DropdownMenuItem(
                                    text = { Text(dimension.toString()) },
                                    onClick = {
                                        viewModel.selectSecondaryDimension(dimension)
                                        viewModel.toggleSecondaryDimensionDropdown()
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

                    // 多维度分析卡片
                    state.dimensionalData.forEach { dimensionalItem ->
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
                                    text = dimensionalItem.label,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                // 趋势图
                                LineChart(
                                    data = LineChartData(
                                        lines = listOf(
                                            LineChartData.Line(
                                                label = "收入",
                                                points = dimensionalItem.incomeData,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            LineChartData.Line(
                                                label = "支出",
                                                points = dimensionalItem.expenseData,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        )
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )

                                // 分布图
                                PieChart(
                                    data = PieChartData(
                                        items = dimensionalItem.distributionData
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )

                                // 分布数据列表
                                dimensionalItem.distributionData.forEach { item ->
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