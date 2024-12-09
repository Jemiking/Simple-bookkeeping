package com.example.myapplication.presentation.transaction.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.myapplication.domain.usecase.transaction.UpdateTransactionUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
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
                // 导航逻辑将在UI层处理
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            getTransactionByIdUseCase(id).collect { transaction ->
                currentTransaction = transaction
                _state.update {
                    it.copy(
                        transaction = transaction,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                updateTransactionUseCase(transaction)
                _state.update {
                    it.copy(
                        transaction = transaction,
                        isEditMode = false,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "更新失败"
                    )
                }
            }
        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            currentTransaction?.let { transaction ->
                try {
                    _state.update { it.copy(isLoading = true) }
                    deleteTransactionUseCase(transaction.id)
                    onEvent(TransactionDetailEvent.NavigateBack)
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "删除失败"
                        )
                    }
                }
            }
        }
    }
} 