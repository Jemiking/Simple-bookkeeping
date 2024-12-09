package com.example.myapplication.domain.model

import com.example.myapplication.data.local.entity.CategoryType
import java.time.YearMonth

data class MonthlyStatistics(
    val yearMonth: YearMonth,
    val totalIncome: Double,
    val totalExpense: Double,
    val categoryStatistics: List<CategoryStatistics>,
    val dailyStatistics: List<DailyStatistics>
) {
    val balance: Double
        get() = totalIncome - totalExpense
}

data class CategoryStatistics(
    val categoryId: Long,
    val categoryName: String,
    val categoryType: CategoryType,
    val categoryColor: String,
    val amount: Double,
    val percentage: Double,
    val count: Int
)

data class DailyStatistics(
    val date: String,
    val income: Double,
    val expense: Double
) {
    val balance: Double
        get() = income - expense
}

data class YearlyStatistics(
    val year: Int,
    val monthlyStatistics: List<MonthlyStatistics>
) {
    val totalIncome: Double
        get() = monthlyStatistics.sumOf { it.totalIncome }
    
    val totalExpense: Double
        get() = monthlyStatistics.sumOf { it.totalExpense }
    
    val balance: Double
        get() = totalIncome - totalExpense
    
    val averageIncome: Double
        get() = if (monthlyStatistics.isNotEmpty()) {
            totalIncome / monthlyStatistics.size
        } else {
            0.0
        }
    
    val averageExpense: Double
        get() = if (monthlyStatistics.isNotEmpty()) {
            totalExpense / monthlyStatistics.size
        } else {
            0.0
        }
}

data class CategoryTrend(
    val categoryId: Long,
    val categoryName: String,
    val categoryType: CategoryType,
    val categoryColor: String,
    val monthlyAmounts: List<MonthlyAmount>
)

data class MonthlyAmount(
    val yearMonth: YearMonth,
    val amount: Double
) 