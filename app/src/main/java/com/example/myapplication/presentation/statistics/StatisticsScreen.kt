package com.example.myapplication.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.main.components.MonthPicker
import com.example.myapplication.presentation.statistics.components.PieChart
import com.example.myapplication.presentation.statistics.components.TrendChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            // TODO: 显示错误提示
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(StatisticsEvent.ToggleChartType) }
                    ) {
                        Icon(
                            imageVector = if (state.showPieChart) {
                                Icons.Default.BarChart
                            } else {
                                Icons.Default.PieChart
                            },
                            contentDescription = "切换图表类型"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份选择器
            MonthPicker(
                selectedMonth = state.selectedMonth,
                onMonthSelected = { month ->
                    viewModel.onEvent(StatisticsEvent.MonthSelected(month))
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 收支类型选择
            TabRow(
                selectedTabIndex = if (state.selectedType == com.example.myapplication.data.local.entity.TransactionType.EXPENSE) 0 else 1
            ) {
                Tab(
                    selected = state.selectedType == com.example.myapplication.data.local.entity.TransactionType.EXPENSE,
                    onClick = {
                        viewModel.onEvent(StatisticsEvent.TypeChanged(
                            com.example.myapplication.data.local.entity.TransactionType.EXPENSE
                        ))
                    }
                ) {
                    Text(
                        text = "支出",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                Tab(
                    selected = state.selectedType == com.example.myapplication.data.local.entity.TransactionType.INCOME,
                    onClick = {
                        viewModel.onEvent(StatisticsEvent.TypeChanged(
                            com.example.myapplication.data.local.entity.TransactionType.INCOME
                        ))
                    }
                ) {
                    Text(
                        text = "收入",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图表区域
            if (state.showPieChart) {
                // 饼图
                PieChart(
                    stats = state.categoryStats,
                    selectedCategoryId = state.selectedCategoryId,
                    onCategorySelected = { categoryId ->
                        viewModel.onEvent(StatisticsEvent.CategorySelected(categoryId))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            } else {
                // 趋势图
                TrendChart(
                    data = state.monthlyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 加载指示器
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
} 