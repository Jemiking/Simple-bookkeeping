package com.example.myapplication.presentation.budget

import java.time.YearMonth

sealed class BudgetEvent {
    // 时间范围选择
    data class MonthSelected(val month: YearMonth) : BudgetEvent()
    
    // 预算编辑
    data class AmountChanged(val amount: String) : BudgetEvent()
    data class CategorySelected(val categoryId: Long?) : BudgetEvent()
    data class ThresholdChanged(val threshold: String) : BudgetEvent()
    data class EnabledChanged(val enabled: Boolean) : BudgetEvent()
    
    // 预算操作
    data object ShowAddDialog : BudgetEvent()
    data object HideAddDialog : BudgetEvent()
    data object ShowDeleteDialog : BudgetEvent()
    data object HideDeleteDialog : BudgetEvent()
    data object SaveBudget : BudgetEvent()
    data object DeleteBudget : BudgetEvent()
    
    // 预算选择
    data class BudgetSelected(val budgetId: Long?) : BudgetEvent()
    
    // 错误处理
    data object DismissError : BudgetEvent()
    
    // 刷新数据
    data object Refresh : BudgetEvent()
} 