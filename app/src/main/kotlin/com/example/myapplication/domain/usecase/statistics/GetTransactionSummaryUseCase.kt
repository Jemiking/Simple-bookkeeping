package com.example.myapplication.domain.usecase.statistics

import com.example.myapplication.domain.model.TransactionSummary
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetTransactionSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Result<TransactionSummary>> {
        return repository.getTransactionSummary(startDate, endDate)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 