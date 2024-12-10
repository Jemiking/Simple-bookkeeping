package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.statistics.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getTransactionSummaryUseCase: GetTransactionSummaryUseCase,
    private val getCategorySummariesUseCase: GetCategorySummariesUseCase,
    private val getTrendUseCase: GetTrendUseCase,
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase,
    private val getYearlyReportUseCase: GetYearlyReportUseCase,
    private val getCustomReportUseCase: GetCustomReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<StatisticsEffect>()
    val effect: SharedFlow<StatisticsEffect> = _effect.asSharedFlow()

    // 缓存最近的数据
    private val cache = LruCache<String, Any>(MAX_CACHE_SIZE)
    
    // 管理正在进行的协程
    private var currentJob: Job? = null

    init {
        loadMonthlyReport(state.value.selectedYear, state.value.selectedMonth)
    }

    override fun onCleared() {
        super.onCleared()
        // 清理缓存和取消所有协程
        cache.evictAll()
        currentJob?.cancel()
    }

    fun onEvent(event: StatisticsEvent) {
        // 取消正在进行的任务
        currentJob?.cancel()
        
        currentJob = when (event) {
            is StatisticsEvent.LoadTransactionSummary -> 
                loadTransactionSummary(event.startDate, event.endDate)
            is StatisticsEvent.LoadCategorySummaries -> 
                loadCategorySummaries(event.startDate, event.endDate)
            is StatisticsEvent.LoadTrend -> 
                loadTrend(event.startDate, event.endDate, event.groupBy)
            is StatisticsEvent.LoadMonthlyReport -> 
                loadMonthlyReport(event.year, event.month)
            is StatisticsEvent.LoadYearlyReport -> 
                loadYearlyReport(event.year)
            is StatisticsEvent.LoadCustomReport -> 
                loadCustomReport(event.startDate, event.endDate)
            is StatisticsEvent.SelectYear -> {
                selectYear(event.year)
                null
            }
            is StatisticsEvent.SelectMonth -> {
                selectMonth(event.month)
                null
            }
            is StatisticsEvent.SelectCustomDateRange -> {
                selectCustomDateRange(event.startDate, event.endDate)
                null
            }
        }
    }

    private fun loadTransactionSummary(startDate: LocalDateTime, endDate: LocalDateTime): Job {
        val cacheKey = "summary_${startDate}_${endDate}"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                transactionSummary = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getTransactionSummaryUseCase(startDate, endDate)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { summary ->
                            cache.put(cacheKey, summary)
                            _state.update { it.copy(
                                transactionSummary = summary,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun loadCategorySummaries(startDate: LocalDateTime, endDate: LocalDateTime): Job {
        val cacheKey = "categories_${startDate}_${endDate}"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                categorySummaries = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCategorySummariesUseCase(startDate, endDate)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { summaries ->
                            cache.put(cacheKey, summaries)
                            _state.update { it.copy(
                                categorySummaries = summaries,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun loadTrend(startDate: LocalDateTime, endDate: LocalDateTime, groupBy: String): Job {
        val cacheKey = "trend_${startDate}_${endDate}_${groupBy}"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                trend = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getTrendUseCase(startDate, endDate, groupBy)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { trend ->
                            cache.put(cacheKey, trend)
                            _state.update { it.copy(
                                trend = trend,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun loadMonthlyReport(year: Int, month: Int): Job {
        val cacheKey = "monthly_${year}_${month}"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                monthlyReport = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMonthlyReportUseCase(year, month)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { report ->
                            cache.put(cacheKey, report)
                            _state.update { it.copy(
                                monthlyReport = report,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun loadYearlyReport(year: Int): Job {
        val cacheKey = "yearly_$year"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                yearlyReport = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getYearlyReportUseCase(year)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { report ->
                            cache.put(cacheKey, report)
                            _state.update { it.copy(
                                yearlyReport = report,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun loadCustomReport(startDate: LocalDateTime, endDate: LocalDateTime): Job {
        val cacheKey = "custom_${startDate}_${endDate}"
        val cachedData = cache.get(cacheKey)
        
        if (cachedData != null) {
            _state.update { it.copy(
                customReport = cachedData,
                isLoading = false,
                error = null
            ) }
            return viewModelScope.launch { 
                _effect.emit(StatisticsEffect.DataLoaded) 
            }
        }

        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getCustomReportUseCase(startDate, endDate)
                .catch { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                    _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    result
                        .onSuccess { report ->
                            cache.put(cacheKey, report)
                            _state.update { it.copy(
                                customReport = report,
                                isLoading = false,
                                error = null
                            ) }
                            _effect.emit(StatisticsEffect.DataLoaded)
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                            _effect.emit(StatisticsEffect.Error(error.message ?: "Unknown error"))
                        }
                }
        }
    }

    private fun selectYear(year: Int) {
        _state.update { it.copy(selectedYear = year) }
        loadYearlyReport(year)
    }

    private fun selectMonth(month: Int) {
        _state.update { it.copy(selectedMonth = month) }
        loadMonthlyReport(state.value.selectedYear, month)
    }

    private fun selectCustomDateRange(startDate: LocalDateTime, endDate: LocalDateTime) {
        _state.update {
            it.copy(
                customStartDate = startDate,
                customEndDate = endDate
            )
        }
        loadCustomReport(startDate, endDate)
    }

    companion object {
        private const val MAX_CACHE_SIZE = 50
    }
}

private class LruCache<K, V>(maxSize: Int) {
    private val cache = LinkedHashMap<K, V>(maxSize, 0.75f, true)
    private val maxSize = maxSize

    @Synchronized
    fun get(key: K): V? = cache[key]

    @Synchronized
    fun put(key: K, value: V) {
        cache[key] = value
        if (cache.size > maxSize) {
            val eldest = cache.entries.first()
            cache.remove(eldest.key)
        }
    }

    @Synchronized
    fun evictAll() = cache.clear()
} 