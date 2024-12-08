package com.example.myapplication.presentation.transaction.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchTransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchTransactionState())
    val state: StateFlow<SearchTransactionState> = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    getTransactionsUseCase()
                        .map { transactions ->
                            transactions.filter { transaction ->
                                val matchesQuery = if (query.isBlank()) {
                                    true
                                } else {
                                    transaction.note?.contains(query, ignoreCase = true) == true ||
                                    transaction.categoryName.contains(query, ignoreCase = true) ||
                                    transaction.accountName.contains(query, ignoreCase = true)
                                }

                                val matchesType = _state.value.selectedType?.let { type ->
                                    transaction.type == type
                                } ?: true

                                val matchesCategory = _state.value.selectedCategoryId?.let { categoryId ->
                                    transaction.categoryId == categoryId
                                } ?: true

                                val matchesAccount = _state.value.selectedAccountId?.let { accountId ->
                                    transaction.accountId == accountId
                                } ?: true

                                val matchesDateRange = (_state.value.startDate == null || transaction.date.isAfter(_state.value.startDate)) &&
                                    (_state.value.endDate == null || transaction.date.isBefore(_state.value.endDate))

                                val matchesAmountRange = (_state.value.minAmount == null || transaction.amount >= _state.value.minAmount) &&
                                    (_state.value.maxAmount == null || transaction.amount <= _state.value.maxAmount)

                                matchesQuery && matchesType && matchesCategory && matchesAccount &&
                                    matchesDateRange && matchesAmountRange
                            }
                        }
                }
                .collect { filteredTransactions ->
                    _state.update { it.copy(transactions = filteredTransactions) }
                }
        }
    }

    fun onEvent(event: SearchTransactionEvent) {
        when (event) {
            is SearchTransactionEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchQuery.value = event.query
            }
            is SearchTransactionEvent.SelectType -> {
                _state.update { it.copy(selectedType = event.type) }
                refreshSearch()
            }
            is SearchTransactionEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategoryId = event.categoryId) }
                refreshSearch()
            }
            is SearchTransactionEvent.SelectAccount -> {
                _state.update { it.copy(selectedAccountId = event.accountId) }
                refreshSearch()
            }
            is SearchTransactionEvent.SetDateRange -> {
                _state.update {
                    it.copy(
                        startDate = event.startDate,
                        endDate = event.endDate
                    )
                }
                refreshSearch()
            }
            is SearchTransactionEvent.SetAmountRange -> {
                _state.update {
                    it.copy(
                        minAmount = event.minAmount,
                        maxAmount = event.maxAmount
                    )
                }
                refreshSearch()
            }
            SearchTransactionEvent.ToggleFilters -> {
                _state.update { it.copy(showFilters = !it.showFilters) }
            }
            SearchTransactionEvent.ClearFilters -> {
                _state.update {
                    it.copy(
                        selectedType = null,
                        selectedCategoryId = null,
                        selectedAccountId = null,
                        startDate = null,
                        endDate = null,
                        minAmount = null,
                        maxAmount = null
                    )
                }
                refreshSearch()
            }
            SearchTransactionEvent.NavigateBack -> {
                // 导航逻辑将在UI层处理
            }
        }
    }

    private fun refreshSearch() {
        searchQuery.value = _state.value.query
    }
} 