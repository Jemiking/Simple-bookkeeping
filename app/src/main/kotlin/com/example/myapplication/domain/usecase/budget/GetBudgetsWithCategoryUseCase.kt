package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.BudgetWithCategory
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetsWithCategoryUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<BudgetWithCategory>> {
        return budgetRepository.getBudgetsWithCategory(yearMonth)
    }
} 