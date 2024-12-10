package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.BudgetCategoryStatistics
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetBudgetCategoryStatisticsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Result<List<BudgetCategoryStatistics>>> {
        return repository.getBudgetCategoryStatistics(startDate, endDate)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 