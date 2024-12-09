package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import javax.inject.Inject

class AddBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Long> {
        return try {
            if (budget.amount <= 0) {
                throw IllegalArgumentException("预算金额必须大于0")
            }
            val id = budgetRepository.insertBudget(budget)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 