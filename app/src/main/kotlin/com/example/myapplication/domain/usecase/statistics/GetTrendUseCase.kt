package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.TrendPoint
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetTrendUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        groupBy: String
    ): Flow<Result<List<TrendPoint>>> {
        return repository.getTrend(startDate, endDate, groupBy)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 