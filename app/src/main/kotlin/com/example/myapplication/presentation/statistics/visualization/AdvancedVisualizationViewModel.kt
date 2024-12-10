package com.example.myapplication.presentation.statistics.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.usecase.statistics.GetCategoryStatisticsUseCase
import com.example.myapplication.domain.usecase.statistics.GetTransactionTrendUseCase
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

data class AdvancedVisualizationState(
    val selectedRange: DateRange = DateRange.LAST_THREE_MONTHS,
    val availableRanges: List<DateRange> = DateRange.values().toList(),
    val showRangeDropdown: Boolean = false,
    val totalIncome: BigDecimal = BigDecimal.ZERO,
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val incomeData: List<LineChartData.Point> = emptyList(),
    val expenseData: List<LineChartData.Point> = emptyList(),
    val incomeCategoryData: List<PieChartData.Item> = emptyList(),
    val expenseCategoryData: List<PieChartData.Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdvancedVisualizationViewModel @Inject constructor(
    private val getTransactionTrendUseCase: GetTransactionTrendUseCase,
    private val getCategoryStatisticsUseCase: GetCategoryStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancedVisualizationState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun selectRange(range: DateRange) {
        _state.update { it.copy(selectedRange = range) }
        loadData()
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

                // 加载趋势数据
                getTransactionTrendUseCase(startDate, endDate)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载趋势数据失败"
                            )
                        }
                    }
                    .collect { trends ->
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
                        val totalIncome = trends.fold(BigDecimal.ZERO) { acc, trend -> acc + trend.income }
                        val totalExpense = trends.fold(BigDecimal.ZERO) { acc, trend -> acc + trend.expense }

                        val maxValue = maxOf(
                            trends.maxOfOrNull { it.income } ?: BigDecimal.ZERO,
                            trends.maxOfOrNull { it.expense } ?: BigDecimal.ZERO
                        )

                        val incomeData = trends.mapIndexed { index, trend ->
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

                        val expenseData = trends.mapIndexed { index, trend ->
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

                        _state.update {
                            it.copy(
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                incomeData = incomeData,
                                expenseData = expenseData
                            )
                        }
                    }

                // 加载分类统计数据
                getCategoryStatisticsUseCase(startDate, endDate)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载分类统计失败"
                            )
                        }
                    }
                    .collect { statistics ->
                        val incomeStats = statistics.filter { it.type == TransactionType.INCOME }
                        val expenseStats = statistics.filter { it.type == TransactionType.EXPENSE }

                        val incomeCategoryData = incomeStats.map { stat ->
                            PieChartData.Item(
                                label = stat.category.name,
                                value = stat.percentage.toFloat(),
                                color = stat.category.color
                            )
                        }

                        val expenseCategoryData = expenseStats.map { stat ->
                            PieChartData.Item(
                                label = stat.category.name,
                                value = stat.percentage.toFloat(),
                                color = stat.category.color
                            )
                        }

                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                incomeCategoryData = incomeCategoryData,
                                expenseCategoryData = expenseCategoryData
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