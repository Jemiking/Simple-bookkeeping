package com.example.myapplication.presentation.budget

import com.example.myapplication.domain.model.*
import java.time.LocalDateTime

data class BudgetState(
    val budgets: List<Budget> = emptyList(),
    val archivedBudgets: List<Budget> = emptyList(),
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val budgetStatistics: BudgetStatistics? = null,
    val selectedBudget: Budget? = null,
    val selectedDate: LocalDateTime = LocalDateTime.now(),
    val selectedInterval: RepeatInterval? = null,
    val selectedGroupBy: String = "day",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class BudgetEvent {
    data class CreateBudget(val budget: Budget) : BudgetEvent()
    data class UpdateBudget(val budget: Budget) : BudgetEvent()
    data class DeleteBudget(val budget: Budget) : BudgetEvent()
    data class LoadBudget(val id: Long) : BudgetEvent()
    object LoadAllBudgets : BudgetEvent()
    object LoadArchivedBudgets : BudgetEvent()
    data class LoadBudgetsByDate(val date: LocalDateTime) : BudgetEvent()
    data class LoadBudgetsByCategory(val categoryId: Long) : BudgetEvent()
    data class LoadRepeatingBudgets(val interval: RepeatInterval) : BudgetEvent()
    data class LoadBudgetStatistics(val startDate: LocalDateTime, val endDate: LocalDateTime) : BudgetEvent()
    data class SelectBudget(val budget: Budget) : BudgetEvent()
    data class SelectDate(val date: LocalDateTime) : BudgetEvent()
    data class SelectInterval(val interval: RepeatInterval?) : BudgetEvent()
    data class SelectGroupBy(val groupBy: String) : BudgetEvent()
}

sealed class BudgetEffect {
    data class BudgetCreated(val id: Long) : BudgetEffect()
    object BudgetUpdated : BudgetEffect()
    object BudgetDeleted : BudgetEffect()
    data class Error(val message: String) : BudgetEffect()
} 