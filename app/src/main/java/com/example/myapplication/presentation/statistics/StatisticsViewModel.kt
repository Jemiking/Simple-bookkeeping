package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.presentation.statistics.components.ChartError
import com.example.myapplication.presentation.statistics.components.MonthlyComparison
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import androidx.collection.LruCache
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val context: android.content.Context
) : ViewModel() {

    companion object {
        private const val BASE_PAGE_SIZE = 50
        private const val MIN_PREFETCH_DISTANCE = 20
        private const val MAX_PREFETCH_DISTANCE = 100
        private const val SCROLL_SPEED_THRESHOLD = 1000L // 毫秒
        private const val NETWORK_SPEED_THRESHOLD = 1000L // 毫秒
    }

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    // 分页状态
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val loadedTransactions = mutableListOf<Transaction>()

    private val loadDataJob: Job? = null
    private var retryCount = 0
    private val maxRetryCount = 3
    private val retryDelayMs = 1000L

    // 内存缓存
    private val memoryCache = LruCache<String, List<Transaction>>(20)
    
    // 磁盘缓存
    private val cacheDir = File(context.cacheDir, "statistics")
    private val maxCacheAge = TimeUnit.HOURS.toMillis(24) // 缓存24小时

    // 预加载配置
    private data class PreloadConfig(
        val pageSize: Int = BASE_PAGE_SIZE,
        val prefetchDistance: Int = MIN_PREFETCH_DISTANCE,
        val enabled: Boolean = true
    )

    private val preloadConfig = MutableStateFlow(PreloadConfig())
    
    // 用户行为跟踪
    private var lastLoadTime = 0L
    private var lastScrollTime = 0L
    private var scrollSpeed = 0f
    private val scrollSpeedHistory = mutableListOf<Float>()
    private var networkSpeed = 0L
    private val networkSpeedHistory = mutableListOf<Long>()

    init {
        // 监听网络状态
        context.registerReceiver(
            object : android.content.BroadcastReceiver() {
                override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                    updatePreloadConfigBasedOnNetwork()
                }
            },
            android.content.IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION)
        )
        
        loadData()
    }

    private fun updatePreloadConfigBasedOnNetwork() {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        
        preloadConfig.update { config ->
            when {
                networkInfo == null -> {
                    config.copy(enabled = false)
                }
                networkInfo.type == android.net.ConnectivityManager.TYPE_WIFI -> {
                    config.copy(
                        enabled = true,
                        prefetchDistance = MAX_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE * 2
                    )
                }
                else -> {
                    config.copy(
                        enabled = true,
                        prefetchDistance = MIN_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE
                    )
                }
            }
        }
    }

    fun onScroll(scrollDelta: Float) {
        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastScrollTime
        
        if (timeDelta > 0) {
            scrollSpeed = scrollDelta / timeDelta
            scrollSpeedHistory.add(scrollSpeed)
            if (scrollSpeedHistory.size > 10) {
                scrollSpeedHistory.removeAt(0)
            }
            
            updatePreloadConfigBasedOnScroll()
        }
        
        lastScrollTime = currentTime
    }

    private fun updatePreloadConfigBasedOnScroll() {
        val averageSpeed = scrollSpeedHistory.average()
        
        preloadConfig.update { config ->
            when {
                averageSpeed > SCROLL_SPEED_THRESHOLD -> {
                    // 快速滚动时增加预加载
                    config.copy(
                        prefetchDistance = MAX_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE * 2
                    )
                }
                averageSpeed > SCROLL_SPEED_THRESHOLD / 2 -> {
                    // 中等速度滚动时适度预加载
                    config.copy(
                        prefetchDistance = (MIN_PREFETCH_DISTANCE + MAX_PREFETCH_DISTANCE) / 2,
                        pageSize = BASE_PAGE_SIZE
                    )
                }
                else -> {
                    // 慢速滚动时减少预加载
                    config.copy(
                        prefetchDistance = MIN_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE
                    )
                }
            }
        }
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.MonthSelected -> {
                _state.update { it.copy(selectedMonth = event.month) }
                loadData()
            }
            is StatisticsEvent.TypeChanged -> {
                _state.update { it.copy(selectedType = event.type) }
                loadData()
            }
            StatisticsEvent.Refresh -> {
                loadData(isRefresh = true)
            }
            StatisticsEvent.RetryLoad -> {
                retryLoadData()
            }
            StatisticsEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadData(isRefresh: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                if (isRefresh) {
                    retryCount = 0
                    clearCache()
                    resetPagingState()
                }
                
                _state.update { it.copy(
                    isLoading = true,
                    error = null
                ) }

                val transactions = loadTransactions(isRefresh)
                processTransactions(transactions)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun resetPagingState() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        loadedTransactions.clear()
    }

    private suspend fun loadTransactions(isRefresh: Boolean = false): List<Transaction> {
        val currentMonth = _state.value.selectedMonth
        val cacheKey = getCacheKey(currentMonth)
        
        // 检查缓存
        if (!isRefresh) {
            memoryCache.get(cacheKey)?.let { cached ->
                Timber.d("Using memory cache for $cacheKey")
                return cached
            }

            loadFromDiskCache(cacheKey)?.let { cached ->
                memoryCache.put(cacheKey, cached)
                Timber.d("Using disk cache for $cacheKey")
                return cached
            }
        }
        
        // 加载新数据
        return loadPagedTransactions()
    }

    private suspend fun loadPagedTransactions(): List<Transaction> {
        if (isLoading || isLastPage) return loadedTransactions
        
        isLoading = true
        val startLoadTime = System.currentTimeMillis()
        val currentMonth = _state.value.selectedMonth
        
        try {
            val config = preloadConfig.value
            if (!config.enabled) return loadedTransactions
            
            // 计算日期范围
            val startDate = currentMonth.minusMonths(5).atDay(1)
            val endDate = currentMonth.atEndOfMonth()
            
            // 加载��页数据
            val offset = currentPage * config.pageSize
            val pagedData = repository.getTransactionsByDateRangePaged(
                startDate = startDate,
                endDate = endDate,
                offset = offset,
                limit = config.pageSize
            ).first()
            
            // 更新加载性能统计
            val loadTime = System.currentTimeMillis() - startLoadTime
            updateLoadingStats(loadTime, pagedData.size)
            
            // 更新分页状态
            if (pagedData.isEmpty()) {
                isLastPage = true
            } else {
                loadedTransactions.addAll(pagedData)
                currentPage++
                
                // 智能预加载判断
                if (shouldPreload(config)) {
                    prefetchNextPage()
                }
            }
            
            return loadedTransactions.sortedBy { it.date }
            
        } finally {
            isLoading = false
        }
    }

    private fun shouldPreload(config: PreloadConfig): Boolean {
        if (!config.enabled || isLastPage) return false
        
        // 检查是否达到预加载距离
        val remainingItems = loadedTransactions.size - (currentPage * config.pageSize)
        if (remainingItems > config.prefetchDistance) return false
        
        // 检查网络性能
        val averageNetworkSpeed = networkSpeedHistory.average()
        if (averageNetworkSpeed > NETWORK_SPEED_THRESHOLD) return false
        
        // 检查内存使用
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        if (usedMemory > maxMemory * 0.8) return false
        
        return true
    }

    private fun updateLoadingStats(loadTime: Long, itemCount: Int) {
        networkSpeed = loadTime / itemCount
        networkSpeedHistory.add(networkSpeed)
        if (networkSpeedHistory.size > 5) {
            networkSpeedHistory.removeAt(0)
        }
        
        // 根据加载性能动态调整预加载配置
        updatePreloadConfigBasedOnPerformance()
    }

    private fun updatePreloadConfigBasedOnPerformance() {
        val averageNetworkSpeed = networkSpeedHistory.average()
        
        preloadConfig.update { config ->
            when {
                averageNetworkSpeed < NETWORK_SPEED_THRESHOLD / 2 -> {
                    // 网络性能好时增加预加载
                    config.copy(
                        prefetchDistance = MAX_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE * 2
                    )
                }
                averageNetworkSpeed > NETWORK_SPEED_THRESHOLD -> {
                    // 网络性能差时减少预加载
                    config.copy(
                        prefetchDistance = MIN_PREFETCH_DISTANCE,
                        pageSize = BASE_PAGE_SIZE
                    )
                }
                else -> config
            }
        }
    }

    private fun prefetchNextPage() {
        if (isLoading || isLastPage) return
        
        viewModelScope.launch {
            try {
                loadPagedTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error prefetching data")
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingMore = true) }
                val newData = loadPagedTransactions()
                processTransactions(newData)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun getCacheKey(month: YearMonth): String {
        return "transactions_${month.year}_${month.monthValue}"
    }

    private fun loadFromDiskCache(key: String): List<Transaction>? {
        val cacheFile = File(cacheDir, "$key.json")
        if (!cacheFile.exists() || !isCacheValid(cacheFile)) {
            return null
        }

        return try {
            val json = cacheFile.readText()
            Json.decodeFromString<List<CachedTransaction>>(json)
                .map { it.toTransaction() }
        } catch (e: Exception) {
            Timber.e(e, "Error reading cache")
            null
        }
    }

    private fun updateCache(key: String, transactions: List<Transaction>) {
        // 更新内存缓存
        memoryCache.put(key, transactions)
        
        // 更新磁盘缓存
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(cacheDir, "$key.json")
                val cachedTransactions = transactions.map { CachedTransaction.fromTransaction(it) }
                val json = Json.encodeToString(cachedTransactions)
                cacheFile.writeText(json)
            } catch (e: Exception) {
                Timber.e(e, "Error writing cache")
            }
        }
    }

    private fun isCacheValid(file: File): Boolean {
        val age = System.currentTimeMillis() - file.lastModified()
        return age < maxCacheAge
    }

    private fun clearCache() {
        // 清理内存缓存
        memoryCache.evictAll()
        
        // 清理磁盘缓存
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cacheDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                Timber.e(e, "Error clearing cache")
            }
        }
    }

    private fun retryLoadData() {
        if (retryCount < maxRetryCount) {
            retryCount++
            viewModelScope.launch {
                delay(retryDelayMs * retryCount)
                loadData()
            }
        } else {
            _state.update { it.copy(
                error = ChartError.Unknown,
                isLoading = false
            ) }
        }
    }

    private fun handleError(error: Exception) {
        val chartError = when (error) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> ChartError.Network
            
            is com.google.gson.JsonParseException,
            is org.json.JSONException,
            is java.text.ParseException -> ChartError.DataFormat
            
            else -> ChartError.Unknown
        }

        _state.update { it.copy(error = chartError) }
        
        // 记录错误日志
        logError(error)
    }

    private fun logError(error: Exception) {
        Timber.e(error, "Statistics data loading error")
    }

    private fun processTransactions(transactions: List<Transaction>) {
        try {
            val currentMonth = _state.value.selectedMonth
            val monthlyData = mutableListOf<MonthlyComparison>()
            
            // 获取最近6个月的数据
            for (i in 5 downTo 0) {
                val month = currentMonth.minusMonths(i.toLong())
                val monthTransactions = transactions.filter { transaction ->
                    val transactionMonth = YearMonth.from(transaction.date)
                    transactionMonth == month
                }
                
                val income = monthTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                    
                val expense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                    
                monthlyData.add(MonthlyComparison(month, income, expense))
            }
            
            // 计算总收支
            val totalIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
                
            val totalExpense = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            // 更新加载状态
            _state.update { it.copy(
                monthlyComparisonData = monthlyData,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                error = null,
                hasMoreData = !isLastPage
            ) }
            
            // 更新缓存
            if (transactions.isNotEmpty()) {
                updateCache(getCacheKey(_state.value.selectedMonth), transactions)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error processing transactions")
            handleError(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearCache()
        resetPagingState()
        scrollSpeedHistory.clear()
        networkSpeedHistory.clear()
    }
}

@Serializable
private data class CachedTransaction(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val date: String,
    val note: String
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            date = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            note = note
        )
    }

    companion object {
        fun fromTransaction(transaction: Transaction): CachedTransaction {
            return CachedTransaction(
                id = transaction.id,
                amount = transaction.amount,
                type = transaction.type,
                categoryId = transaction.categoryId,
                accountId = transaction.accountId,
                date = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                note = transaction.note
            )
        }
    }
}

data class StatisticsState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedType: TransactionType? = null,
    val showPieChart: Boolean = true,
    val isLoading: Boolean = false,
    val error: ChartError? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryStatistics: Map<String, Double> = emptyMap(),
    val dailyStatistics: Map<LocalDate, Double> = emptyMap(),
    val chartData: List<Pair<Float, Float>> = emptyList(),
    val hasMoreData: Boolean = true
)

sealed class StatisticsEvent {
    data class MonthSelected(val month: YearMonth) : StatisticsEvent()
    data class TypeChanged(val type: TransactionType?) : StatisticsEvent()
    data object Refresh : StatisticsEvent()
    data object RetryLoad : StatisticsEvent()
    data object DismissError : StatisticsEvent()
} 