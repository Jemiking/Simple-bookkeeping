package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state

    init {
        loadCurrentMonthData()
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.SelectMonth -> {
                _state.update { it.copy(selectedMonth = event.yearMonth) }
                loadMonthData(event.yearMonth)
            }
            is StatisticsEvent.SelectTransactionType -> {
                _state.update { it.copy(selectedType = event.type) }
                updateChartData()
            }
            StatisticsEvent.ToggleChartType -> {
                _state.update { it.copy(showPieChart = !it.showPieChart) }
            }
        }
    }

    private fun loadCurrentMonthData() {
        loadMonthData(YearMonth.now())
    }

    private fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val startDate = yearMonth.atDay(1)
                val endDate = yearMonth.atEndOfMonth()
                
                transactionRepository.getTransactionsByDateRange(startDate, endDate)
                    .collect { transactions ->
                        processTransactions(transactions)
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun processTransactions(transactions: List<Transaction>) {
        // 计算总收入和支出
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // 按分类统计
        val categoryStats = transactions
            .groupBy { it.categoryId }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount }
            }

        // 生成每日趋势数据
        val dailyStats = generateDailyStats(transactions)

        _state.update {
            it.copy(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                categoryStatistics = categoryStats,
                dailyStatistics = dailyStats
            )
        }

        updateChartData()
    }

    private fun generateDailyStats(transactions: List<Transaction>): Map<LocalDate, Double> {
        val currentMonth = _state.value.selectedMonth
        val result = mutableMapOf<LocalDate, Double>()

        // 初始化当月所有日期
        for (day in 1..currentMonth.lengthOfMonth()) {
            result[currentMonth.atDay(day)] = 0.0
        }

        // 统计每日数据
        transactions.forEach { transaction ->
            val date = transaction.date.toLocalDate()
            result[date] = result.getOrDefault(date, 0.0) + when (transaction.type) {
                TransactionType.INCOME -> transaction.amount
                TransactionType.EXPENSE -> -transaction.amount
                else -> 0.0
            }
        }

        return result
    }

    private fun updateChartData() {
        val currentState = _state.value
        val selectedType = currentState.selectedType
        val dailyStats = currentState.dailyStatistics

        // 根据选择的类型过滤数据
        val filteredData = dailyStats.entries
            .sortedBy { it.key }
            .map { (date, amount) ->
                when (selectedType) {
                    TransactionType.INCOME -> maxOf(amount, 0.0)
                    TransactionType.EXPENSE -> maxOf(-amount, 0.0)
                    else -> amount
                }
            }

        // 转换为图表数据点
        val chartData = filteredData.mapIndexed { index, amount ->
            Pair(index.toFloat(), amount.toFloat())
        }

        _state.update { it.copy(chartData = chartData) }
    }
}

data class StatisticsState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedType: TransactionType? = null,
    val showPieChart: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryStatistics: Map<String, Double> = emptyMap(),
    val dailyStatistics: Map<LocalDate, Double> = emptyMap(),
    val chartData: List<Pair<Float, Float>> = emptyList()
)

sealed class StatisticsEvent {
    data class SelectMonth(val yearMonth: YearMonth) : StatisticsEvent()
    data class SelectTransactionType(val type: TransactionType?) : StatisticsEvent()
    data object ToggleChartType : StatisticsEvent()
} 