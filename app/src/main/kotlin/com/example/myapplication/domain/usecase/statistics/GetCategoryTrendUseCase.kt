package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.CategoryTrend
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryTrendUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(
        categoryId: Long,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<CategoryTrend> {
        return statisticsRepository.getCategoryTrend(
            categoryId = categoryId,
            startYearMonth = startYearMonth,
            endYearMonth = endYearMonth
        )
    }
} 