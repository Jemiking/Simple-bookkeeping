package com.example.myapplication.presentation.statistics

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.transaction.GetTransactionStatsUseCase.CategoryStat
import com.example.myapplication.presentation.statistics.components.MonthlyAmount
import com.example.myapplication.presentation.statistics.components.MonthlyComparison
import java.time.YearMonth

data class StatisticsState(
    // 时间范围
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    
    // 统计数据
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val monthlyData: List<MonthlyAmount> = emptyList(),
    val monthlyComparisonData: List<MonthlyComparison> = emptyList(),
    
    // 图表相关
    val showPieChart: Boolean = true,
    val selectedCategoryId: Long? = null,
    
    // UI状态
    val isLoading: Boolean = false,
    val error: String? = null
) 