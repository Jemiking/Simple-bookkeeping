package com.example.myapplication.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.BudgetProgress
import com.example.myapplication.domain.usecase.budget.GetBudgetProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetProgressUseCase: GetBudgetProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BudgetEffect>()
    val effect: SharedFlow<BudgetEffect> = _effect.asSharedFlow()

    init {
        loadBudgets()
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.SelectMonth -> {
                _state.update { it.copy(selectedMonth = event.yearMonth) }
                loadBudgets()
            }
            is BudgetEvent.Refresh -> loadBudgets()
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getBudgetProgressUseCase(state.value.selectedMonth).collect { budgets ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            budgets = budgets.filter { it.categoryId != 0L },
                            totalBudget = budgets.find { it.categoryId == 0L }
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(BudgetEffect.ShowError(e.message ?: "加载预算失败"))
            }
        }
    }
}

data class BudgetState(
    val isLoading: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now(),
    val budgets: List<BudgetProgress> = emptyList(),
    val totalBudget: BudgetProgress? = null
)

sealed class BudgetEffect {
    data class ShowError(val message: String) : BudgetEffect()
}

sealed class BudgetEvent {
    data class SelectMonth(val yearMonth: YearMonth) : BudgetEvent()
    object Refresh : BudgetEvent()
} 