package com.example.myapplication.presentation.transaction.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionsUseCase
import com.example.myapplication.domain.usecase.transaction.UpdateTransactionUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionDetailState())
    val state: StateFlow<TransactionDetailState> = _state.asStateFlow()

    private var currentTransaction: Transaction? = null

    init {
        savedStateHandle.get<Long>("transactionId")?.let { transactionId ->
            onEvent(TransactionDetailEvent.LoadTransaction(transactionId))
        }
    }

    fun onEvent(event: TransactionDetailEvent) {
        when (event) {
            is TransactionDetailEvent.LoadTransaction -> {
                loadTransaction(event.id)
            }
            is TransactionDetailEvent.UpdateTransaction -> {
                updateTransaction(event.transaction)
            }
            TransactionDetailEvent.DeleteTransaction -> {
                deleteTransaction()
            }
            TransactionDetailEvent.ToggleEditMode -> {
                _state.update { it.copy(isEditMode = !it.isEditMode) }
            }
            TransactionDetailEvent.ShowDeleteConfirmation -> {
                _state.update { it.copy(showDeleteConfirmation = true) }
            }
            TransactionDetailEvent.HideDeleteConfirmation -> {
                _state.update { it.copy(showDeleteConfirmation = false) }
            }
            TransactionDetailEvent.NavigateBack -> {
                // 导航逻辑将在UI��处理
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // 这里需要添加获取单个交易的用例
            // 暂时使用getAllTransactions并过滤
            getTransactionsUseCase().collect { transactions ->
                val transaction = transactions.find { it.id == id }
                if (transaction != null) {
                    currentTransaction = transaction
                    _state.update {
                        it.copy(
                            transaction = transaction,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "找不到该交易记录"
                        )
                    }
                }
            }
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            updateTransactionUseCase(transaction).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                transaction = transaction,
                                isEditMode = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.copy(error = result.message)
                        }
                    }
                    is Result.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            currentTransaction?.let { transaction ->
                deleteTransactionUseCase(transaction).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            onEvent(TransactionDetailEvent.NavigateBack)
                        }
                        is Result.Error -> {
                            _state.update {
                                it.copy(error = result.message)
                            }
                        }
                        is Result.Loading -> {
                            _state.update {
                                it.copy(isLoading = true)
                            }
                        }
                    }
                }
            }
        }
    }
} 