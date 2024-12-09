package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.MonthlyStatistics
import com.example.myapplication.domain.usecase.statistics.GetYearlyStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YearlyStatisticsViewModel @Inject constructor(
    private val getYearlyStatisticsUseCase: GetYearlyStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(YearlyStatisticsState())
    val state: StateFlow<YearlyStatisticsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<YearlyStatisticsEffect>()
    val effect: SharedFlow<YearlyStatisticsEffect> = _effect.asSharedFlow()

    init {
        loadStatistics()
    }

    fun onEvent(event: YearlyStatisticsEvent) {
        when (event) {
            is YearlyStatisticsEvent.SelectYear -> {
                _state.update { it.copy(selectedYear = event.year) }
                loadStatistics()
            }
            YearlyStatisticsEvent.Refresh -> loadStatistics()
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getYearlyStatisticsUseCase(state.value.selectedYear).collect { statistics ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            totalIncome = statistics.totalIncome,
                            totalExpense = statistics.totalExpense,
                            balance = statistics.balance,
                            averageIncome = statistics.averageIncome,
                            averageExpense = statistics.averageExpense,
                            monthlyStatistics = statistics.monthlyStatistics
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(YearlyStatisticsEffect.ShowError(e.message ?: "加载统计数据失败"))
            }
        }
    }
}

data class YearlyStatisticsState(
    val isLoading: Boolean = false,
    val selectedYear: Int = java.time.Year.now().value,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val averageIncome: Double = 0.0,
    val averageExpense: Double = 0.0,
    val monthlyStatistics: List<MonthlyStatistics> = emptyList()
)

sealed class YearlyStatisticsEffect {
    data class ShowError(val message: String) : YearlyStatisticsEffect()
}

sealed class YearlyStatisticsEvent {
    data class SelectYear(val year: Int) : YearlyStatisticsEvent()
    object Refresh : YearlyStatisticsEvent()
} 