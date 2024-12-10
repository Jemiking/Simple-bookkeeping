package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface StatisticsRepository {
    fun getMonthlyStatistics(yearMonth: YearMonth): Flow<MonthlyStatistics>
    
    fun getYearlyStatistics(year: Int): Flow<YearlyStatistics>
    
    fun getCategoryTrend(
        categoryId: Long,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<CategoryTrend>
    
    fun getMultiCategoryTrend(
        categoryIds: List<Long>,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<List<CategoryTrend>>
} 