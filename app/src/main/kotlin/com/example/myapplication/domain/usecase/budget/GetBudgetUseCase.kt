package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(yearMonth: YearMonth, categoryId: Long): Flow<Budget?> {
        return budgetRepository.getBudget(yearMonth, categoryId)
    }
} 