package com.example.myapplication.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.usecase.budget.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val createBudgetUseCase: CreateBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val getAllActiveBudgetsUseCase: GetAllActiveBudgetsUseCase,
    private val getAllArchivedBudgetsUseCase: GetAllArchivedBudgetsUseCase,
    private val getBudgetsByDateUseCase: GetBudgetsByDateUseCase,
    private val getBudgetsByCategoryUseCase: GetBudgetsByCategoryUseCase,
    private val getRepeatingBudgetsUseCase: GetRepeatingBudgetsUseCase,
    private val getAllBudgetProgressUseCase: GetAllBudgetProgressUseCase,
    private val getBudgetStatisticsUseCase: GetBudgetStatisticsUseCase,
    private val getBudgetCategoryStatisticsUseCase: GetBudgetCategoryStatisticsUseCase,
    private val getBudgetTrendUseCase: GetBudgetTrendUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BudgetEffect>()
    val effect: SharedFlow<BudgetEffect> = _effect.asSharedFlow()

    init {
        loadAllBudgets()
        loadBudgetProgress()
        loadBudgetStatistics()
    }

    fun onEvent(event: BudgetEvent) {
        when (event) {
            is BudgetEvent.CreateBudget -> createBudget(event.budget)
            is BudgetEvent.UpdateBudget -> updateBudget(event.budget)
            is BudgetEvent.DeleteBudget -> deleteBudget(event.budget)
            is BudgetEvent.LoadBudget -> loadBudget(event.id)
            is BudgetEvent.LoadAllBudgets -> loadAllBudgets()
            is BudgetEvent.LoadArchivedBudgets -> loadArchivedBudgets()
            is BudgetEvent.LoadBudgetsByDate -> loadBudgetsByDate(event.date)
            is BudgetEvent.LoadBudgetsByCategory -> loadBudgetsByCategory(event.categoryId)
            is BudgetEvent.LoadRepeatingBudgets -> loadRepeatingBudgets(event.interval)
            is BudgetEvent.LoadBudgetStatistics -> loadBudgetStatistics(event.startDate, event.endDate)
            is BudgetEvent.SelectBudget -> selectBudget(event.budget)
            is BudgetEvent.SelectDate -> selectDate(event.date)
            is BudgetEvent.SelectInterval -> selectInterval(event.interval)
            is BudgetEvent.SelectGroupBy -> selectGroupBy(event.groupBy)
        }
    }

    private fun createBudget(budget: Budget) {
        viewModelScope.launch {
            createBudgetUseCase(budget)
                .onSuccess { id ->
                    _effect.emit(BudgetEffect.BudgetCreated(id))
                    loadAllBudgets()
                    loadBudgetProgress()
                    loadBudgetStatistics()
                }
                .onFailure { error ->
                    _effect.emit(BudgetEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            updateBudgetUseCase(budget)
                .onSuccess {
                    _effect.emit(BudgetEffect.BudgetUpdated)
                    loadAllBudgets()
                    loadBudgetProgress()
                    loadBudgetStatistics()
                }
                .onFailure { error ->
                    _effect.emit(BudgetEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            deleteBudgetUseCase(budget)
                .onSuccess {
                    _effect.emit(BudgetEffect.BudgetDeleted)
                    loadAllBudgets()
                    loadBudgetProgress()
                    loadBudgetStatistics()
                }
                .onFailure { error ->
                    _effect.emit(BudgetEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun loadBudget(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllActiveBudgetsUseCase()
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            val budget = budgets.find { it.id == id }
                            _state.update { it.copy(
                                selectedBudget = budget,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadAllBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllActiveBudgetsUseCase()
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            _state.update { it.copy(
                                budgets = budgets,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadArchivedBudgets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllArchivedBudgetsUseCase()
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            _state.update { it.copy(
                                archivedBudgets = budgets,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadBudgetsByDate(date: LocalDateTime) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getBudgetsByDateUseCase(date)
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            _state.update { it.copy(
                                budgets = budgets,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadBudgetsByCategory(categoryId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getBudgetsByCategoryUseCase(categoryId)
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            _state.update { it.copy(
                                budgets = budgets,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadRepeatingBudgets(interval: com.example.myapplication.domain.model.RepeatInterval) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getRepeatingBudgetsUseCase(interval)
                .collect { result ->
                    result
                        .onSuccess { budgets ->
                            _state.update { it.copy(
                                budgets = budgets,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadBudgetProgress() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllBudgetProgressUseCase()
                .collect { result ->
                    result
                        .onSuccess { progress ->
                            _state.update { it.copy(
                                budgetProgress = progress,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadBudgetStatistics(
        startDate: LocalDateTime = state.value.selectedDate.withDayOfMonth(1),
        endDate: LocalDateTime = state.value.selectedDate.plusMonths(1).withDayOfMonth(1).minusNanos(1)
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getBudgetStatisticsUseCase(startDate, endDate)
                .collect { result ->
                    result
                        .onSuccess { statistics ->
                            _state.update { it.copy(
                                budgetStatistics = statistics,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun selectBudget(budget: Budget) {
        _state.update { it.copy(selectedBudget = budget) }
    }

    private fun selectDate(date: LocalDateTime) {
        _state.update { it.copy(selectedDate = date) }
        loadBudgetsByDate(date)
        loadBudgetStatistics()
    }

    private fun selectInterval(interval: com.example.myapplication.domain.model.RepeatInterval?) {
        _state.update { it.copy(selectedInterval = interval) }
        interval?.let { loadRepeatingBudgets(it) } ?: loadAllBudgets()
    }

    private fun selectGroupBy(groupBy: String) {
        _state.update { it.copy(selectedGroupBy = groupBy) }
        loadBudgetStatistics()
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