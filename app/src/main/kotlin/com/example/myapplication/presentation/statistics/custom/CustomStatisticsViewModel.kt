package com.example.myapplication.presentation.statistics.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.StatisticsDimension
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.usecase.statistics.GetCustomStatisticsUseCase
import com.example.myapplication.presentation.components.chart.LineChartData
import com.example.myapplication.presentation.components.chart.PieChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class DateRange(val months: Int) {
    LAST_THREE_MONTHS(3),
    LAST_SIX_MONTHS(6),
    LAST_TWELVE_MONTHS(12);

    override fun toString(): String = when (this) {
        LAST_THREE_MONTHS -> "近3个月"
        LAST_SIX_MONTHS -> "近6个月"
        LAST_TWELVE_MONTHS -> "近12个月"
    }
}

data class CustomStatisticsState(
    val selectedDimension: StatisticsDimension = StatisticsDimension.CATEGORY,
    val availableDimensions: List<StatisticsDimension> = StatisticsDimension.values().toList(),
    val showDimensionDropdown: Boolean = false,
    val selectedRange: DateRange = DateRange.LAST_THREE_MONTHS,
    val availableRanges: List<DateRange> = DateRange.values().toList(),
    val showRangeDropdown: Boolean = false,
    val showIncome: Boolean = true,
    val showExpense: Boolean = true,
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val incomeData: List<LineChartData.Point> = emptyList(),
    val expenseData: List<LineChartData.Point> = emptyList(),
    val incomeDistributionData: List<PieChartData.Item> = emptyList(),
    val expenseDistributionData: List<PieChartData.Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CustomStatisticsViewModel @Inject constructor(
    private val getCustomStatisticsUseCase: GetCustomStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CustomStatisticsState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun selectDimension(dimension: StatisticsDimension) {
        _state.update { it.copy(selectedDimension = dimension) }
        loadData()
    }

    fun selectRange(range: DateRange) {
        _state.update { it.copy(selectedRange = range) }
        loadData()
    }

    fun toggleDimensionDropdown() {
        _state.update { it.copy(showDimensionDropdown = !it.showDimensionDropdown) }
    }

    fun toggleRangeDropdown() {
        _state.update { it.copy(showRangeDropdown = !it.showRangeDropdown) }
    }

    fun toggleIncomeVisible() {
        _state.update { it.copy(showIncome = !it.showIncome) }
    }

    fun toggleExpenseVisible() {
        _state.update { it.copy(showExpense = !it.showExpense) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val endDate = LocalDate.now()
                val startDate = endDate.minusMonths(_state.value.selectedRange.months.toLong())

                getCustomStatisticsUseCase(
                    startDate = startDate,
                    endDate = endDate,
                    dimension = _state.value.selectedDimension
                ).catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载数据失败"
                        )
                    }
                }.collect { statistics ->
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
                    val totalIncome = statistics.trends
                        .fold(BigDecimal.ZERO) { acc, trend -> acc + trend.income }
                    val totalExpense = statistics.trends
                        .fold(BigDecimal.ZERO) { acc, trend -> acc + trend.expense }

                    val maxValue = maxOf(
                        statistics.trends.maxOfOrNull { it.income } ?: BigDecimal.ZERO,
                        statistics.trends.maxOfOrNull { it.expense } ?: BigDecimal.ZERO
                    )

                    val incomeData = statistics.trends.mapIndexed { index, trend ->
                        LineChartData.Point(
                            x = index.toFloat(),
                            y = if (maxValue > BigDecimal.ZERO) {
                                trend.income.toFloat() / maxValue.toFloat()
                            } else {
                                0f
                            },
                            label = trend.date.format(dateFormatter)
                        )
                    }

                    val expenseData = statistics.trends.mapIndexed { index, trend ->
                        LineChartData.Point(
                            x = index.toFloat(),
                            y = if (maxValue > BigDecimal.ZERO) {
                                trend.expense.toFloat() / maxValue.toFloat()
                            } else {
                                0f
                            },
                            label = trend.date.format(dateFormatter)
                        )
                    }

                    val incomeDistributionData = statistics.distributions
                        .filter { it.type == TransactionType.INCOME }
                        .map { distribution ->
                            PieChartData.Item(
                                label = distribution.label,
                                value = distribution.percentage.toFloat(),
                                color = distribution.color
                            )
                        }

                    val expenseDistributionData = statistics.distributions
                        .filter { it.type == TransactionType.EXPENSE }
                        .map { distribution ->
                            PieChartData.Item(
                                label = distribution.label,
                                value = distribution.percentage.toFloat(),
                                color = distribution.color
                            )
                        }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            incomeData = incomeData,
                            expenseData = expenseData,
                            incomeDistributionData = incomeDistributionData,
                            expenseDistributionData = expenseDistributionData
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载数据失败"
                    )
                }
            }
        }
    }
} 