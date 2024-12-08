package com.example.myapplication.presentation.budget

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.Category
import java.time.YearMonth

data class BudgetState(
    // 时间范围
    val selectedMonth: YearMonth = YearMonth.now(),
    
    // 预算数据
    val overallBudget: Budget? = null,
    val categoryBudgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    
    // 编辑状态
    val isEditing: Boolean = false,
    val editingBudget: EditingBudget = EditingBudget(),
    
    // UI状态
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val selectedBudgetId: Long? = null
)

data class EditingBudget(
    val id: Long = 0,
    val amount: String = "",
    val categoryId: Long? = null,
    val notifyThreshold: String = "0.8",
    val isEnabled: Boolean = true,
    
    // 验证错误
    val amountError: String? = null,
    val thresholdError: String? = null
) 