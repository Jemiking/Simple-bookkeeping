package com.example.myapplication.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.usecase.budget.AddBudgetUseCase
import com.example.myapplication.domain.usecase.budget.DeleteBudgetUseCase
import com.example.myapplication.domain.usecase.budget.GetBudgetsUseCase
import com.example.myapplication.domain.usecase.budget.UpdateBudgetUseCase
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgets: GetBudgetsUseCase,
    private val addBudget: AddBudgetUseCase,
    private val updateBudget: UpdateBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
    private val getCategories: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state = combine(
        _state,
        getBudgets(),
        getCategories(type = TransactionType.EXPENSE)
    ) { state, budgets, categories ->
        state.copy(
            overallBudget = budgets.find { it.categoryId == null },
            categoryBudgets = budgets.filter { it.categoryId != null },
            categories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetState()
    )

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.MonthSelected -> {
                _state.value = _state.value.copy(
                    selectedMonth = event.month,
                    selectedBudgetId = null
                )
            }
            is BudgetEvent.AmountChanged -> {
                val amount = event.amount.trim()
                val amountError = when {
                    amount.isEmpty() -> "请输入预算金额"
                    amount.toDoubleOrNull() == null -> "请输入有效的金额"
                    amount.toDouble() <= 0 -> "预算金额必须大于0"
                    else -> null
                }
                _state.value = _state.value.copy(
                    editingBudget = _state.value.editingBudget.copy(
                        amount = amount,
                        amountError = amountError
                    )
                )
            }
            is BudgetEvent.CategorySelected -> {
                _state.value = _state.value.copy(
                    editingBudget = _state.value.editingBudget.copy(
                        categoryId = event.categoryId
                    )
                )
            }
            is BudgetEvent.ThresholdChanged -> {
                val threshold = event.threshold.trim()
                val thresholdError = when {
                    threshold.isEmpty() -> "请输入提醒阈值"
                    threshold.toDoubleOrNull() == null -> "请输入有效的阈值"
                    threshold.toDouble() <= 0 || threshold.toDouble() > 1 -> "阈值必须在0到1之间"
                    else -> null
                }
                _state.value = _state.value.copy(
                    editingBudget = _state.value.editingBudget.copy(
                        notifyThreshold = threshold,
                        thresholdError = thresholdError
                    )
                )
            }
            is BudgetEvent.EnabledChanged -> {
                _state.value = _state.value.copy(
                    editingBudget = _state.value.editingBudget.copy(
                        isEnabled = event.enabled
                    )
                )
            }
            BudgetEvent.ShowAddDialog -> {
                _state.value = _state.value.copy(
                    isAddDialogVisible = true,
                    editingBudget = EditingBudget()
                )
            }
            BudgetEvent.HideAddDialog -> {
                _state.value = _state.value.copy(
                    isAddDialogVisible = false,
                    editingBudget = EditingBudget()
                )
            }
            BudgetEvent.ShowDeleteDialog -> {
                _state.value = _state.value.copy(
                    isDeleteDialogVisible = true
                )
            }
            BudgetEvent.HideDeleteDialog -> {
                _state.value = _state.value.copy(
                    isDeleteDialogVisible = false
                )
            }
            BudgetEvent.SaveBudget -> {
                saveBudget()
            }
            BudgetEvent.DeleteBudget -> {
                deleteBudget()
            }
            is BudgetEvent.BudgetSelected -> {
                _state.value = _state.value.copy(
                    selectedBudgetId = event.budgetId
                )
            }
            BudgetEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
            BudgetEvent.Refresh -> {
                // 刷新数据由Flow自动处理
            }
        }
    }

    private fun saveBudget() {
        viewModelScope.launch {
            val budget = _state.value.editingBudget
            
            // 验证输入
            if (budget.amountError != null || budget.thresholdError != null) {
                return@launch
            }
            
            try {
                val budgetModel = Budget(
                    id = budget.id,
                    amount = budget.amount.toDouble(),
                    categoryId = budget.categoryId,
                    yearMonth = _state.value.selectedMonth,
                    notifyThreshold = budget.notifyThreshold.toDouble(),
                    isEnabled = budget.isEnabled
                )
                
                if (budget.id == 0L) {
                    addBudget(budgetModel)
                } else {
                    updateBudget(budgetModel)
                }
                
                _state.value = _state.value.copy(
                    isAddDialogVisible = false,
                    editingBudget = EditingBudget()
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message
                )
            }
        }
    }

    private fun deleteBudget() {
        viewModelScope.launch {
            try {
                val selectedBudget = state.value.categoryBudgets.find { 
                    it.id == state.value.selectedBudgetId 
                } ?: state.value.overallBudget
                
                if (selectedBudget != null) {
                    deleteBudget(selectedBudget)
                }
                
                _state.value = _state.value.copy(
                    isDeleteDialogVisible = false,
                    selectedBudgetId = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message
                )
            }
        }
    }
} 