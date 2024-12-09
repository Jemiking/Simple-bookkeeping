package com.example.myapplication.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.usecase.budget.*
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    private val getBudgetUseCase: GetBudgetUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetDetailState())
    val state: StateFlow<BudgetDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BudgetDetailEffect>()
    val effect: SharedFlow<BudgetDetailEffect> = _effect.asSharedFlow()

    init {
        loadCategories()
    }

    fun loadBudget(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getBudgetUseCase(state.value.yearMonth, id).collect { budget ->
                    budget?.let {
                        _state.update { state ->
                            state.copy(
                                isLoading = false,
                                budget = it,
                                categoryId = it.categoryId,
                                amount = it.amount.toString(),
                                note = it.note
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "加载预算失败"))
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase(CategoryType.EXPENSE).collect { categories ->
                    _state.update { it.copy(categories = categories) }
                }
            } catch (e: Exception) {
                _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "加载分类失败"))
            }
        }
    }

    fun onEvent(event: BudgetDetailEvent) {
        when (event) {
            is BudgetDetailEvent.UpdateYearMonth -> {
                _state.update { it.copy(yearMonth = event.yearMonth) }
            }
            is BudgetDetailEvent.UpdateCategory -> {
                _state.update { it.copy(categoryId = event.categoryId) }
            }
            is BudgetDetailEvent.UpdateAmount -> {
                val amountError = when {
                    event.amount.isEmpty() -> "请输入预算金额"
                    event.amount.toDoubleOrNull() == null -> "请输入有效的金额"
                    event.amount.toDouble() <= 0 -> "预算金额必须大于0"
                    else -> null
                }
                _state.update {
                    it.copy(
                        amount = event.amount,
                        amountError = amountError
                    )
                }
            }
            is BudgetDetailEvent.UpdateNote -> {
                _state.update { it.copy(note = event.note) }
            }
            BudgetDetailEvent.SaveBudget -> saveBudget()
            BudgetDetailEvent.DeleteBudget -> deleteBudget()
        }
    }

    private fun saveBudget() {
        viewModelScope.launch {
            try {
                if (state.value.amountError != null) {
                    return@launch
                }

                val budget = Budget(
                    id = state.value.budget?.id ?: 0,
                    categoryId = state.value.categoryId,
                    yearMonth = state.value.yearMonth,
                    amount = state.value.amount.toDouble(),
                    note = state.value.note
                )

                val result = if (budget.id == 0L) {
                    addBudgetUseCase(budget)
                } else {
                    updateBudgetUseCase(budget)
                }

                result.onSuccess {
                    _effect.emit(BudgetDetailEffect.NavigateBack)
                }.onFailure { e ->
                    _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "保存失败"))
                }
            } catch (e: Exception) {
                _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "保存失败"))
            }
        }
    }

    private fun deleteBudget() {
        viewModelScope.launch {
            try {
                state.value.budget?.let { budget ->
                    deleteBudgetUseCase(budget)
                        .onSuccess {
                            _effect.emit(BudgetDetailEffect.NavigateBack)
                        }
                        .onFailure { e ->
                            _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "删除失败"))
                        }
                }
            } catch (e: Exception) {
                _effect.emit(BudgetDetailEffect.ShowError(e.message ?: "删除失败"))
            }
        }
    }
}

data class BudgetDetailState(
    val isLoading: Boolean = false,
    val budget: Budget? = null,
    val categories: List<Category> = emptyList(),
    val yearMonth: YearMonth = YearMonth.now(),
    val categoryId: Long = 0,
    val amount: String = "",
    val amountError: String? = null,
    val note: String = ""
)

sealed class BudgetDetailEffect {
    object NavigateBack : BudgetDetailEffect()
    data class ShowError(val message: String) : BudgetDetailEffect()
}

sealed class BudgetDetailEvent {
    data class UpdateYearMonth(val yearMonth: YearMonth) : BudgetDetailEvent()
    data class UpdateCategory(val categoryId: Long) : BudgetDetailEvent()
    data class UpdateAmount(val amount: String) : BudgetDetailEvent()
    data class UpdateNote(val note: String) : BudgetDetailEvent()
    object SaveBudget : BudgetDetailEvent()
    object DeleteBudget : BudgetDetailEvent()
} 