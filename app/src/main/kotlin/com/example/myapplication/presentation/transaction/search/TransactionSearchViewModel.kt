package com.example.myapplication.presentation.transaction.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.SearchHistory
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionFilter
import com.example.myapplication.domain.repository.SearchHistoryRepository
import com.example.myapplication.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionSearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionSearchState())
    val state = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null
    private var historyJob: Job? = null
    private var loadMoreJob: Job? = null

    // 缓存搜索历史
    private val historyCache = mutableMapOf<String, List<String>>()
    
    // 分页参数
    private var currentPage = 0
    private var hasMoreData = true
    private val pageSize = 20

    init {
        // 加载搜索历史
        loadSearchHistory()

        // 自动搜索和搜索建议
        setupSearchFlow()
    }

    private fun loadSearchHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            searchHistoryRepository.getSearchHistory()
                .catch { e ->
                    _state.update {
                        it.copy(error = e.message ?: "加载搜索历史失败")
                    }
                }
                .collectLatest { history ->
                    _state.update {
                        it.copy(searchHistory = history)
                    }
                }
        }
    }

    private fun setupSearchFlow() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                suggestedQueries = emptyList(),
                                transactions = emptyList()
                            )
                        }
                    } else {
                        // 重置分页参数
                        resetPagination()
                        
                        // 从缓存获取搜索建议
                        val suggestions = historyCache[query] ?: run {
                            // 缓存未命中,从仓库获取
                            searchHistoryRepository.searchHistory(query)
                                .catch { e ->
                                    _state.update {
                                        it.copy(error = e.message ?: "获取搜索建议失败")
                                    }
                                }
                                .first()
                                .also { suggestions ->
                                    // 更新缓存
                                    historyCache[query] = suggestions
                                }
                        }
                        
                        _state.update {
                            it.copy(suggestedQueries = suggestions)
                        }
                        
                        // 执行搜索
                        search(query, state.value.filter)
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        queryFlow.value = query
        if (query.isBlank() && state.value.filter == TransactionFilter()) {
            resetPagination()
            _state.update { it.copy(transactions = emptyList()) }
        }
    }

    fun onFilterChange(filter: TransactionFilter) {
        _state.update { it.copy(filter = filter) }
        resetPagination()
        search(state.value.query, filter)
    }

    private fun resetPagination() {
        currentPage = 0
        hasMoreData = true
        loadMoreJob?.cancel()
    }

    fun loadMore() {
        if (!hasMoreData || state.value.isLoading) return
        
        val query = state.value.query
        val filter = state.value.filter
        
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoadingMore = true) }
                
                val nextPage = currentPage + 1
                val offset = nextPage * pageSize
                
                val newTransactions = transactionRepository.searchTransactionsPaged(
                    query = query,
                    offset = offset,
                    limit = pageSize
                )
                .map { transactions ->
                    transactions.filter { transaction ->
                        when {
                            filter.startDate != null && transaction.date < filter.startDate -> false
                            filter.endDate != null && transaction.date > filter.endDate -> false
                            filter.minAmount != null && transaction.amount < filter.minAmount -> false
                            filter.maxAmount != null && transaction.amount > filter.maxAmount -> false
                            filter.categoryIds.isNotEmpty() && transaction.categoryId !in filter.categoryIds -> false
                            filter.accountIds.isNotEmpty() && transaction.accountId !in filter.accountIds -> false
                            else -> true
                        }
                    }
                }
                .first()

                if (newTransactions.isEmpty()) {
                    hasMoreData = false
                } else {
                    currentPage = nextPage
                    _state.update {
                        it.copy(
                            transactions = it.transactions + newTransactions,
                            isLoadingMore = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "加载更多失败"
                    )
                }
            }
        }
    }

    private fun search(query: String, filter: TransactionFilter) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                transactionRepository.searchTransactionsPaged(
                    query = query,
                    offset = 0,
                    limit = pageSize
                )
                .map { transactions ->
                    transactions.filter { transaction ->
                        when {
                            filter.startDate != null && transaction.date < filter.startDate -> false
                            filter.endDate != null && transaction.date > filter.endDate -> false
                            filter.minAmount != null && transaction.amount < filter.minAmount -> false
                            filter.maxAmount != null && transaction.amount > filter.maxAmount -> false
                            filter.categoryIds.isNotEmpty() && transaction.categoryId !in filter.categoryIds -> false
                            filter.accountIds.isNotEmpty() && transaction.accountId !in filter.accountIds -> false
                            else -> true
                        }
                    }
                }
                .catch { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "搜索失败"
                        )
                    }
                }
                .collectLatest { transactions ->
                    _state.update {
                        it.copy(
                            transactions = transactions,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "搜索失败"
                    )
                }
            }
        }
    }

    fun retry() {
        val query = state.value.query
        val filter = state.value.filter
        
        if (state.value.isLoadingMore) {
            loadMore()
        } else {
            resetPagination()
            search(query, filter)
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        historyJob?.cancel()
        loadMoreJob?.cancel()
        historyCache.clear()
    }
}

data class TransactionSearchState(
    val query: String = "",
    val filter: TransactionFilter = TransactionFilter(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val searchHistory: List<SearchHistory> = emptyList(),
    val suggestedQueries: List<String> = emptyList(),
    val error: String? = null
) 