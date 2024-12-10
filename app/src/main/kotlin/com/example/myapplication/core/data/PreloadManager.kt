package com.example.myapplication.core.data

import android.content.Context
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 缓存数据
    private val transactionCache = MutableStateFlow<List<Transaction>>(emptyList())
    private val accountCache = MutableStateFlow<List<Account>>(emptyList())
    private val categoryCache = MutableStateFlow<List<Category>>(emptyList())
    
    // 预加载状态
    private val _preloadState = MutableStateFlow<PreloadState>(PreloadState.NotStarted)
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()
    
    // 缓存过期时间
    private var lastUpdateTime: LocalDateTime? = null
    private val cacheValidityDuration = java.time.Duration.ofMinutes(5)

    // 性能监控
    private val loadingTimes = ConcurrentHashMap<String, Long>()
    private val cacheHits = AtomicInteger(0)
    private val cacheMisses = AtomicInteger(0)

    init {
        startPreloading()
        setupPeriodicRefresh()
    }

    private fun setupPeriodicRefresh() {
        scope.launch {
            while (isActive) {
                delay(cacheValidityDuration.toMillis())
                if (!isCacheValid()) {
                    refreshCache()
                }
            }
        }
    }

    fun startPreloading() {
        scope.launch {
            try {
                _preloadState.value = PreloadState.Loading(0)
                
                // 并行预加载
                coroutineScope {
                    val jobs = listOf(
                        async { preloadCategories() },
                        async { preloadAccounts() },
                        async { preloadRecentTransactions() }
                    )
                    
                    jobs.forEachIndexed { index, job ->
                        job.await()
                        _preloadState.value = PreloadState.Loading(((index + 1) * 100) / jobs.size)
                    }
                }
                
                // 预热数据库查询
                warmupQueries()
                
                _preloadState.value = PreloadState.Completed
                lastUpdateTime = LocalDateTime.now()
            } catch (e: Exception) {
                _preloadState.value = PreloadState.Error(e.message ?: "预加载失败")
            }
        }
    }

    private suspend fun preloadCategories() = measureTime("categories") {
        database.categoryDao().getAllCategories()
            .catch { e -> throw PreloadException("加载分类数据失败", e) }
            .collect { entities ->
                categoryCache.value = entities.map { it.toDomain() }
            }
    }

    private suspend fun preloadAccounts() = measureTime("accounts") {
        database.accountDao().getAllAccounts()
            .catch { e -> throw PreloadException("加载账户数据失败", e) }
            .collect { entities ->
                accountCache.value = entities.map { it.toDomain() }
            }
    }

    private suspend fun preloadRecentTransactions() = measureTime("transactions") {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusDays(7)
        
        database.transactionDao().getTransactionsByDateRange(
            startDate.toString(),
            endDate.toString()
        )
        .catch { e -> throw PreloadException("加载交易数据失败", e) }
        .collect { entities ->
            transactionCache.value = entities.map { it.toDomain() }
        }
    }

    private suspend fun warmupQueries() = measureTime("warmup") {
        withContext(Dispatchers.IO) {
            database.queryExecutor.execute {
                // 执行一些常用查询来预热数据库
                database.runInTransaction {
                    database.transactionDao().getRecentTransactions(10)
                    database.accountDao().getTotalBalance()
                    database.categoryDao().getTopCategories(5)
                }
            }
        }
    }

    fun getTransactionCache(): Flow<List<Transaction>> {
        recordCacheAccess(transactionCache.value.isNotEmpty())
        return transactionCache.asStateFlow()
    }
    
    fun getAccountCache(): Flow<List<Account>> {
        recordCacheAccess(accountCache.value.isNotEmpty())
        return accountCache.asStateFlow()
    }
    
    fun getCategoryCache(): Flow<List<Category>> {
        recordCacheAccess(categoryCache.value.isNotEmpty())
        return categoryCache.asStateFlow()
    }

    fun isCacheValid(): Boolean {
        val now = LocalDateTime.now()
        return lastUpdateTime?.let { lastUpdate ->
            java.time.Duration.between(lastUpdate, now) <= cacheValidityDuration
        } ?: false
    }

    fun invalidateCache() {
        scope.launch {
            transactionCache.value = emptyList()
            accountCache.value = emptyList()
            categoryCache.value = emptyList()
            lastUpdateTime = null
            startPreloading()
        }
    }

    fun refreshCache() {
        if (!isCacheValid()) {
            invalidateCache()
        }
    }

    fun clearCache() {
        scope.launch {
            transactionCache.value = emptyList()
            accountCache.value = emptyList()
            categoryCache.value = emptyList()
            lastUpdateTime = null
            _preloadState.value = PreloadState.NotStarted
        }
    }

    private suspend fun <T> measureTime(operation: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        try {
            return block()
        } finally {
            val endTime = System.currentTimeMillis()
            loadingTimes[operation] = endTime - startTime
        }
    }

    private fun recordCacheAccess(isHit: Boolean) {
        if (isHit) {
            cacheHits.incrementAndGet()
        } else {
            cacheMisses.incrementAndGet()
        }
    }

    fun getPerformanceMetrics(): Map<String, Any> {
        return mapOf(
            "loadingTimes" to loadingTimes.toMap(),
            "cacheHits" to cacheHits.get(),
            "cacheMisses" to cacheMisses.get(),
            "hitRate" to calculateHitRate()
        )
    }

    private fun calculateHitRate(): Double {
        val hits = cacheHits.get()
        val total = hits + cacheMisses.get()
        return if (total > 0) hits.toDouble() / total else 0.0
    }

    fun resetPerformanceMetrics() {
        loadingTimes.clear()
        cacheHits.set(0)
        cacheMisses.set(0)
    }
}

sealed class PreloadState {
    object NotStarted : PreloadState()
    data class Loading(val progress: Int) : PreloadState()
    object Completed : PreloadState()
    data class Error(val message: String) : PreloadState()
}

class PreloadException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) 