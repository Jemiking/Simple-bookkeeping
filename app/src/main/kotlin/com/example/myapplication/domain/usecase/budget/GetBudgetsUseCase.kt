package com.example.myapplication.domain.usecase.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(
        yearMonth: YearMonth = YearMonth.now()
    ): Flow<List<Budget>> {
        return budgetRepository.getBudgetsByMonth(yearMonth)
            .catch { e ->
                e.printStackTrace()
                emit(emptyList())
            }
            .map { budgets ->
                // 按超支状态和金额排序
                budgets.sortedWith(
                    compareByDescending<Budget> { it.isExceeded }
                        .thenByDescending { it.spendingPercentage }
                )
            }
    }

    // 获取总预算
    fun getOverallBudget(yearMonth: YearMonth = YearMonth.now()): Flow<Budget?> {
        return budgetRepository.getOverallBudgetByMonth(yearMonth)
            .catch { e ->
                e.printStackTrace()
                emit(null)
            }
    }

    // 获取超支预算
    fun getExceededBudgets(yearMonth: YearMonth = YearMonth.now()): Flow<List<Budget>> {
        return budgetRepository.getBudgetsExceedingThreshold(yearMonth)
            .catch { e ->
                e.printStackTrace()
                emit(emptyList())
            }
            .map { budgets ->
                budgets.sortedByDescending { it.spendingPercentage }
            }
    }
} 