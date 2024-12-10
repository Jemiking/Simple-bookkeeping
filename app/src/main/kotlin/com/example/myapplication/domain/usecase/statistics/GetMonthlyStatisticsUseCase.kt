package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.MonthlyStatistics
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetMonthlyStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<MonthlyStatistics> {
        return statisticsRepository.getMonthlyStatistics(yearMonth)
    }
} 