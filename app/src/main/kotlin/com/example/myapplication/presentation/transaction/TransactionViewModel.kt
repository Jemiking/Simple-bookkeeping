package com.example.myapplication.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val isLastPage: Boolean = false
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionListState())
    val state = _state.asStateFlow()

    private val pageSize = 20

    init {
        loadTransactions()
    }

    fun loadMore() {
        if (!state.value.isLoading && !state.value.isLastPage) {
            loadTransactions(state.value.currentPage + 1)
        }
    }

    fun refresh() {
        _state.update { 
            it.copy(
                transactions = emptyList(),
                currentPage = 0,
                isLastPage = false,
                error = null
            )
        }
        loadTransactions()
    }

    private fun loadTransactions(page: Int = 0) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getTransactionsUseCase(
                    offset = page * pageSize,
                    limit = pageSize
                )
                    .catch { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载交易记录失败"
                            )
                        }
                    }
                    .collect { transactions ->
                        _state.update {
                            it.copy(
                                transactions = if (page == 0) {
                                    transactions
                                } else {
                                    it.transactions + transactions
                                },
                                isLoading = false,
                                error = null,
                                currentPage = page,
                                isLastPage = transactions.size < pageSize
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载交易记录失败"
                    )
                }
            }
        }
    }
} 
} 