package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> {
        return try {
            repository.updateBudget(budget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 