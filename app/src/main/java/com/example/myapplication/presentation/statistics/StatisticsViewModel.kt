package com.example.myapplication.presentation.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.statistics.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    // 分页状态
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val loadedTransactions = mutableListOf<Transaction>()

    // 缓存配置
    private val cacheConfig = CacheConfig(
        maxMemCacheSize = 50,
        maxDiskCacheSize = 100 * 1024 * 1024, // 100MB
        maxCacheAge = TimeUnit.HOURS.toMillis(24)
    )

    // 内存缓存
    private val memoryCache = object : LinkedHashMap<String, List<Transaction>>(
        cacheConfig.maxMemCacheSize,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, List<Transaction>>): Boolean {
            return size > cacheConfig.maxMemCacheSize
        }
    }

    // 磁盘缓存目录
    private val cacheDir by lazy {
        File(context.cacheDir, "statistics").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    init {
        // 清理过期缓存
        cleanExpiredCache()
        // 加载初始数据
        loadInitialData()
    }

    fun loadMoreData() {
        if (isLoading || isLastPage) return
        loadData()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            currentPage = 0
            isLastPage = false
            loadedTransactions.clear()
            memoryCache.clear()
            loadData()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val cachedData = getFromCache("initial_data")
                if (cachedData != null) {
                    loadedTransactions.addAll(cachedData)
                    _state.update { it.copy(
                        transactions = cachedData,
                        isLoading = false
                    ) }
                } else {
                    loadData()
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "加载失败",
                    isLoading = false
                ) }
            }
        }
    }

    private fun loadData() {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            try {
                val result = getStatisticsUseCase(
                    page = currentPage,
                    pageSize = PAGE_SIZE
                )

                result.onSuccess { transactions ->
                    if (transactions.isEmpty()) {
                        isLastPage = true
                    } else {
                        loadedTransactions.addAll(transactions)
                        currentPage++
                        saveToCache("initial_data", loadedTransactions)
                    }

                    _state.update { it.copy(
                        transactions = loadedTransactions.toList(),
                        isLoading = false,
                        error = null
                    ) }
                }.onFailure { e ->
                    _state.update { it.copy(
                        error = e.message ?: "加载失败",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "加载失败",
                    isLoading = false
                ) }
            } finally {
                isLoading = false
            }
        }
    }

    private fun getFromCache(key: String): List<Transaction>? {
        // 1. 先从内存缓存获取
        memoryCache[key]?.let { return it }

        // 2. 尝试从磁盘缓存获取
        return try {
            val cacheFile = File(cacheDir, key)
            if (cacheFile.exists() && isCacheValid(cacheFile)) {
                val data = cacheFile.readText().let {
                    // 反序列化逻辑
                    emptyList() // TODO: 实现反序列化
                }
                memoryCache[key] = data
                data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveToCache(key: String, data: List<Transaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. 保存到内存缓存
                memoryCache[key] = data

                // 2. 保存到磁盘缓存
                val cacheFile = File(cacheDir, key)
                cacheFile.writeText(
                    // 序列化逻辑
                    "" // TODO: 实现序列化
                )
            } catch (e: Exception) {
                // 缓存失败不影响主流程
                e.printStackTrace()
            }
        }
    }

    private fun cleanExpiredCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cacheDir.listFiles()?.forEach { file ->
                    if (!isCacheValid(file)) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isCacheValid(file: File): Boolean {
        val lastModified = file.lastModified()
        val now = System.currentTimeMillis()
        return now - lastModified <= cacheConfig.maxCacheAge
    }

    override fun onCleared() {
        super.onCleared()
        // 清理资源
        viewModelScope.cancel()
        memoryCache.clear()
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

private data class CacheConfig(
    val maxMemCacheSize: Int,
    val maxDiskCacheSize: Long,
    val maxCacheAge: Long
)

data class StatisticsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
) 