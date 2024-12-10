package com.example.myapplication.presentation.statistics

import com.example.myapplication.domain.model.*
import java.time.LocalDateTime

data class StatisticsState(
    val transactionSummary: TransactionSummary? = null,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val monthlyReport: MonthlyReport? = null,
    val yearlyReport: YearlyReport? = null,
    val customReport: CustomReport? = null,
    val selectedYear: Int = LocalDateTime.now().year,
    val selectedMonth: Int = LocalDateTime.now().monthValue,
    val customStartDate: LocalDateTime = LocalDateTime.now().minusMonths(1),
    val customEndDate: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class StatisticsEvent {
    data class LoadTransactionSummary(val startDate: LocalDateTime, val endDate: LocalDateTime) : StatisticsEvent()
    data class LoadCategorySummaries(val startDate: LocalDateTime, val endDate: LocalDateTime) : StatisticsEvent()
    data class LoadTrend(val startDate: LocalDateTime, val endDate: LocalDateTime, val groupBy: String) : StatisticsEvent()
    data class LoadMonthlyReport(val year: Int, val month: Int) : StatisticsEvent()
    data class LoadYearlyReport(val year: Int) : StatisticsEvent()
    data class LoadCustomReport(val startDate: LocalDateTime, val endDate: LocalDateTime) : StatisticsEvent()
    data class SelectYear(val year: Int) : StatisticsEvent()
    data class SelectMonth(val month: Int) : StatisticsEvent()
    data class SelectCustomDateRange(val startDate: LocalDateTime, val endDate: LocalDateTime) : StatisticsEvent()
}

sealed class StatisticsEffect {
    object DataLoaded : StatisticsEffect()
    data class Error(val message: String) : StatisticsEffect()
} 