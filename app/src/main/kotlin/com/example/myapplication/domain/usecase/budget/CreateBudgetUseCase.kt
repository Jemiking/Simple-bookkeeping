package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import javax.inject.Inject

class CreateBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Long> {
        return try {
            val id = repository.createBudget(budget)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 