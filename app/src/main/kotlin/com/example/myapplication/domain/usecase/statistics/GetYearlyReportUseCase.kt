package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.YearlyReport
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetYearlyReportUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(year: Int): Flow<Result<YearlyReport>> {
        return repository.getYearlyReport(year)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 