package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.CategoryStatistics
import com.example.myapplication.domain.model.DailyStatistics
import com.example.myapplication.domain.usecase.statistics.GetMonthlyStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MonthlyStatisticsViewModel @Inject constructor(
    private val getMonthlyStatisticsUseCase: GetMonthlyStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MonthlyStatisticsState())
    val state: StateFlow<MonthlyStatisticsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MonthlyStatisticsEffect>()
    val effect: SharedFlow<MonthlyStatisticsEffect> = _effect.asSharedFlow()

    init {
        loadStatistics()
    }

    fun onEvent(event: MonthlyStatisticsEvent) {
        when (event) {
            is MonthlyStatisticsEvent.SelectMonth -> {
                _state.update { it.copy(selectedMonth = event.yearMonth) }
                loadStatistics()
            }
            MonthlyStatisticsEvent.Refresh -> loadStatistics()
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getMonthlyStatisticsUseCase(state.value.selectedMonth).collect { statistics ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            totalIncome = statistics.totalIncome,
                            totalExpense = statistics.totalExpense,
                            balance = statistics.balance,
                            categoryStatistics = statistics.categoryStatistics,
                            dailyStatistics = statistics.dailyStatistics
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(MonthlyStatisticsEffect.ShowError(e.message ?: "加载统计数据失败"))
            }
        }
    }
}

data class MonthlyStatisticsState(
    val isLoading: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryStatistics: List<CategoryStatistics> = emptyList(),
    val dailyStatistics: List<DailyStatistics> = emptyList()
)

sealed class MonthlyStatisticsEffect {
    data class ShowError(val message: String) : MonthlyStatisticsEffect()
}

sealed class MonthlyStatisticsEvent {
    data class SelectMonth(val yearMonth: YearMonth) : MonthlyStatisticsEvent()
    object Refresh : MonthlyStatisticsEvent()
} 