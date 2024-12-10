package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBudgetsByCategoryUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(categoryId: Long): Flow<Result<List<Budget>>> {
        return repository.getBudgetsByCategory(categoryId)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 