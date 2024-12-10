package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.YearlyStatistics
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetYearlyStatisticsUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    operator fun invoke(year: Int): Flow<YearlyStatistics> {
        return statisticsRepository.getYearlyStatistics(year)
    }
} 