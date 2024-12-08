package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Unit> {
        return try {
            // 验证预算金额
            if (budget.amount <= 0) {
                return Result.error(IllegalArgumentException("预算金额必须大于0"))
            }

            // 验证通知阈值
            if (budget.notifyThreshold != null && (budget.notifyThreshold <= 0 || budget.notifyThreshold > 1)) {
                return Result.error(IllegalArgumentException("通知阈值必须在0到1之间"))
            }

            // 获取当前支出
            val currentSpending = budgetRepository.getCurrentSpending(
                budget.categoryId,
                budget.yearMonth
            ).first()

            // 检查新预算是否小于当前支出
            if (budget.amount < currentSpending) {
                return Result.error(IllegalStateException(
                    "新预算金额(${budget.amount})不能小于当前支出($currentSpending)"
                ))
            }

            // 更新预算
            budgetRepository.updateBudget(budget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 更新预算启用状态
    suspend fun updateEnabled(budget: Budget, enabled: Boolean): Result<Unit> {
        return try {
            val updatedBudget = budget.copy(isEnabled = enabled)
            budgetRepository.updateBudget(updatedBudget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 更新预算通知阈值
    suspend fun updateNotifyThreshold(budget: Budget, threshold: Double?): Result<Unit> {
        return try {
            // 验证通知阈值
            if (threshold != null && (threshold <= 0 || threshold > 1)) {
                return Result.error(IllegalArgumentException("通知阈值必须在0到1之间"))
            }

            val updatedBudget = budget.copy(notifyThreshold = threshold)
            budgetRepository.updateBudget(updatedBudget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 