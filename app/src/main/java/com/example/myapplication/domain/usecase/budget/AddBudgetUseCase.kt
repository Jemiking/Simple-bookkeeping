package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

class AddBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(budget: Budget): Result<Long> {
        return try {
            // 验证预算金额
            if (budget.amount <= 0) {
                return Result.error(IllegalArgumentException("预算金额必须大于0"))
            }

            // 验证通知阈值
            if (budget.notifyThreshold != null && (budget.notifyThreshold <= 0 || budget.notifyThreshold > 1)) {
                return Result.error(IllegalArgumentException("通知阈值必须在0到1之间"))
            }

            // 如果是分类预算，验证分类是否存在且为支出类型
            if (budget.categoryId != null) {
                val category = categoryRepository.getCategoryById(budget.categoryId)
                    ?: return Result.error(IllegalArgumentException("分类不存在"))

                if (category.type != TransactionType.EXPENSE) {
                    return Result.error(IllegalArgumentException("只能为支出分类设置预算"))
                }

                // 检查是否已存在该分类的预算
                val existingBudget = budgetRepository.getBudgetByCategoryAndMonth(
                    budget.categoryId,
                    budget.yearMonth
                ).first()

                if (existingBudget != null) {
                    return Result.error(IllegalStateException("该分类在${budget.yearMonth}已存在预算"))
                }
            } else {
                // 检查是否已存在总预算
                val existingOverallBudget = budgetRepository.getOverallBudgetByMonth(
                    budget.yearMonth
                ).first()

                if (existingOverallBudget != null) {
                    return Result.error(IllegalStateException("${budget.yearMonth}已存在总预算"))
                }
            }

            // 添加预算
            val id = budgetRepository.addBudget(budget)
            Result.success(id)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 