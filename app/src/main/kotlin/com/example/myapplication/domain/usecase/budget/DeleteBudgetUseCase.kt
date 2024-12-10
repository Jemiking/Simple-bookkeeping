package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> {
        return try {
            repository.deleteBudget(budget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 