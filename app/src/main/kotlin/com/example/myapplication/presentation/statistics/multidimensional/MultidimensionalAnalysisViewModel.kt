package com.example.myapplication.presentation.statistics.multidimensional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.StatisticsDimension
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.usecase.statistics.GetMultidimensionalStatisticsUseCase
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

data class DimensionalItem(
    val label: String,
    val incomeData: List<LineChartData.Point>,
    val expenseData: List<LineChartData.Point>,
    val distributionData: List<PieChartData.Item>
)

data class MultidimensionalAnalysisState(
    val selectedPrimaryDimension: StatisticsDimension = StatisticsDimension.CATEGORY,
    val selectedSecondaryDimension: StatisticsDimension = StatisticsDimension.TIME,
    val availableDimensions: List<StatisticsDimension> = StatisticsDimension.values().toList(),
    val showPrimaryDimensionDropdown: Boolean = false,
    val showSecondaryDimensionDropdown: Boolean = false,
    val selectedRange: DateRange = DateRange.LAST_THREE_MONTHS,
    val availableRanges: List<DateRange> = DateRange.values().toList(),
    val showRangeDropdown: Boolean = false,
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val dimensionalData: List<DimensionalItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MultidimensionalAnalysisViewModel @Inject constructor(
    private val getMultidimensionalStatisticsUseCase: GetMultidimensionalStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MultidimensionalAnalysisState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun selectPrimaryDimension(dimension: StatisticsDimension) {
        _state.update {
            it.copy(
                selectedPrimaryDimension = dimension,
                selectedSecondaryDimension = if (dimension == it.selectedSecondaryDimension) {
                    StatisticsDimension.values().first { d -> d != dimension }
                } else {
                    it.selectedSecondaryDimension
                }
            )
        }
        loadData()
    }

    fun selectSecondaryDimension(dimension: StatisticsDimension) {
        _state.update { it.copy(selectedSecondaryDimension = dimension) }
        loadData()
    }

    fun selectRange(range: DateRange) {
        _state.update { it.copy(selectedRange = range) }
        loadData()
    }

    fun togglePrimaryDimensionDropdown() {
        _state.update { it.copy(showPrimaryDimensionDropdown = !it.showPrimaryDimensionDropdown) }
    }

    fun toggleSecondaryDimensionDropdown() {
        _state.update { it.copy(showSecondaryDimensionDropdown = !it.showSecondaryDimensionDropdown) }
    }

    fun toggleRangeDropdown() {
        _state.update { it.copy(showRangeDropdown = !it.showRangeDropdown) }
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

                getMultidimensionalStatisticsUseCase(
                    startDate = startDate,
                    endDate = endDate,
                    primaryDimension = _state.value.selectedPrimaryDimension,
                    secondaryDimension = _state.value.selectedSecondaryDimension
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

                    val dimensionalData = statistics.dimensionalData.map { dimensionalItem ->
                        val maxValue = maxOf(
                            dimensionalItem.trends.maxOfOrNull { it.income } ?: BigDecimal.ZERO,
                            dimensionalItem.trends.maxOfOrNull { it.expense } ?: BigDecimal.ZERO
                        )

                        val incomeData = dimensionalItem.trends.mapIndexed { index, trend ->
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

                        val expenseData = dimensionalItem.trends.mapIndexed { index, trend ->
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

                        val distributionData = dimensionalItem.distributions.map { distribution ->
                            PieChartData.Item(
                                label = distribution.label,
                                value = distribution.percentage.toFloat(),
                                color = distribution.color
                            )
                        }

                        DimensionalItem(
                            label = dimensionalItem.label,
                            incomeData = incomeData,
                            expenseData = expenseData,
                            distributionData = distributionData
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            dimensionalData = dimensionalData
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