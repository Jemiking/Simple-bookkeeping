package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.MonthlyReport
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<Result<MonthlyReport>> {
        return repository.getMonthlyReport(year, month)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 