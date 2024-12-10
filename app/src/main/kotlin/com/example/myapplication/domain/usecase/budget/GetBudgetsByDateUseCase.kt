package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetBudgetsByDateUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(date: LocalDateTime): Flow<Result<List<Budget>>> {
        return repository.getActiveBudgetsByDate(date)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 