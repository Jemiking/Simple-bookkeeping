package com.example.myapplication.presentation.transaction.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.SearchHistoryRepository
import com.example.myapplication.domain.usecase.transaction.SearchTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionSearchState(
    val query: String = "",
    val filter: TransactionFilter = TransactionFilter(),
    val transactions: List<Transaction> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val suggestedQueries: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFilter: Boolean = false
)

@HiltViewModel
class TransactionSearchViewModel @Inject constructor(
    private val searchTransactionsUseCase: SearchTransactionsUseCase,
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionSearchState())
    val state = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        // 加载搜索历史
        viewModelScope.launch {
            searchHistoryRepository.getSearchHistory()
                .catch { e ->
                    _state.update {
                        it.copy(error = e.message ?: "加载搜索历史失败")
                    }
                }
                .collect { history ->
                    _state.update {
                        it.copy(searchHistory = history)
                    }
                }
        }

        // 自动搜索和搜索建议
        viewModelScope.launch {
            queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                suggestedQueries = emptyList(),
                                transactions = emptyList()
                            )
                        }
                    } else {
                        // 获取搜索建议
                        searchHistoryRepository.searchHistory(query)
                            .catch { e ->
                                _state.update {
                                    it.copy(error = e.message ?: "获取搜索建议失败")
                                }
                            }
                            .collect { suggestions ->
                                _state.update {
                                    it.copy(suggestedQueries = suggestions)
                                }
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
            _state.update { it.copy(transactions = emptyList()) }
        }
    }

    fun onFilterChange(filter: TransactionFilter) {
        _state.update { it.copy(filter = filter) }
        search(state.value.query, filter)
    }

    fun toggleFilter() {
        _state.update { it.copy(showFilter = !it.showFilter) }
    }

    fun search() {
        viewModelScope.launch {
            if (state.value.query.isNotBlank()) {
                searchHistoryRepository.addSearchHistory(state.value.query)
            }
        }
        search(state.value.query, state.value.filter)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearSearchHistory()
        }
    }

    private fun search(query: String, filter: TransactionFilter) {
        if (query.isBlank() && filter == TransactionFilter()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                searchTransactionsUseCase(query, filter)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "搜索失败"
                            )
                        }
                    }
                    .collect { transactions ->
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
} 