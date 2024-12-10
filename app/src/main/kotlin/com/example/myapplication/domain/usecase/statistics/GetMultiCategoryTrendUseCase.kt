package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.CategoryTrend
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetMultiCategoryTrendUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(
        categoryIds: List<Long>,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<List<CategoryTrend>> {
        return statisticsRepository.getMultiCategoryTrend(
            categoryIds = categoryIds,
            startYearMonth = startYearMonth,
            endYearMonth = endYearMonth
        )
    }
} 