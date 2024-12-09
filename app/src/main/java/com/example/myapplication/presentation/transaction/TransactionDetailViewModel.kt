package com.example.myapplication.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.myapplication.domain.usecase.transaction.GetRelatedTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailState(
    val transaction: Transaction? = null,
    val relatedTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getRelatedTransactionsUseCase: GetRelatedTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = checkNotNull(savedStateHandle["transactionId"])

    private val _state = MutableStateFlow(TransactionDetailState())
    val state = _state.asStateFlow()

    init {
        loadTransaction()
    }

    fun loadTransaction() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getTransactionByIdUseCase(transactionId)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载交易详情失败"
                            )
                        }
                    }
                    .collect { transaction ->
                        _state.update {
                            it.copy(
                                transaction = transaction,
                                isLoading = false,
                                error = null
                            )
                        }
                        loadRelatedTransactions(transaction)
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载交易详情失败"
                    )
                }
            }
        }
    }

    private fun loadRelatedTransactions(transaction: Transaction?) {
        if (transaction == null) return

        viewModelScope.launch {
            try {
                getRelatedTransactionsUseCase(
                    categoryId = transaction.categoryId,
                    accountId = transaction.accountId,
                    excludeId = transaction.id
                )
                    .catch { e ->
                        _state.update {
                            it.copy(error = e.message ?: "加载相关交易失败")
                        }
                    }
                    .collect { transactions ->
                        _state.update {
                            it.copy(relatedTransactions = transactions)
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "加载相关交易失败")
                }
            }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                deleteTransactionUseCase(transactionId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "删除交易失败"
                    )
                }
            }
        }
    }
} 