package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> {
        return try {
            // 获取当前支出
            val currentSpending = budgetRepository.getCurrentSpending(
                budget.categoryId,
                budget.yearMonth
            ).first()

            // 如果已有支出，不允许删除
            if (currentSpending > 0) {
                return Result.error(IllegalStateException(
                    "该预算已有支出记录(${currentSpending}元)，不能删除"
                ))
            }

            // 如果是总预算，检查是否有分类预算
            if (budget.categoryId == null) {
                val budgets = budgetRepository.getBudgetsByMonth(budget.yearMonth).first()
                if (budgets.any { it.categoryId != null }) {
                    return Result.error(IllegalStateException(
                        "请先删除所有分类预算"
                    ))
                }
            }

            // 删除预算
            budgetRepository.deleteBudget(budget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 