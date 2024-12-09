package com.example.myapplication.presentation.transaction.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.transaction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchOperationState(
    val transactions: List<Transaction> = emptyList(),
    val selectedTransactions: Set<Long> = emptySet(),
    val isAllSelected: Boolean = false,
    val showOperationMenu: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showCategorySelection: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BatchOperationViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BatchOperationState())
    val state = _state.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getTransactionsUseCase()
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
                        error = e.message ?: "加载交易记录失败"
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase()
                    .catch { e ->
                        _state.update {
                            it.copy(error = e.message ?: "加载分类失败")
                        }
                    }
                    .collect { categories ->
                        _state.update { it.copy(categories = categories) }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "加载分类失败")
                }
            }
        }
    }

    fun toggleSelection(transactionId: Long) {
        _state.update {
            val newSelection = if (it.selectedTransactions.contains(transactionId)) {
                it.selectedTransactions - transactionId
            } else {
                it.selectedTransactions + transactionId
            }
            it.copy(
                selectedTransactions = newSelection,
                isAllSelected = newSelection.size == it.transactions.size
            )
        }
    }

    fun toggleSelectAll() {
        _state.update {
            if (it.isAllSelected) {
                it.copy(
                    selectedTransactions = emptySet(),
                    isAllSelected = false
                )
            } else {
                it.copy(
                    selectedTransactions = it.transactions.map { t -> t.id }.toSet(),
                    isAllSelected = true
                )
            }
        }
    }

    fun clearSelection() {
        _state.update {
            it.copy(
                selectedTransactions = emptySet(),
                isAllSelected = false
            )
        }
    }

    fun toggleOperationMenu() {
        _state.update { it.copy(showOperationMenu = !it.showOperationMenu) }
    }

    fun showDeleteConfirmation() {
        _state.update { it.copy(showDeleteConfirmation = true) }
    }

    fun dismissDeleteConfirmation() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    fun showCategorySelection() {
        _state.update { it.copy(showCategorySelection = true) }
    }

    fun dismissCategorySelection() {
        _state.update { it.copy(showCategorySelection = false) }
    }

    fun selectCategory(categoryId: Long) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun startBatchEdit() {
        // TODO: 导航到批量编辑界面
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                state.value.selectedTransactions.forEach { transactionId ->
                    deleteTransactionUseCase(transactionId)
                }
                clearSelection()
                loadTransactions()
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

    fun confirmCategoryChange() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val categoryId = state.value.selectedCategoryId ?: return@launch
                val category = state.value.categories.find { it.id == categoryId } ?: return@launch

                state.value.transactions
                    .filter { state.value.selectedTransactions.contains(it.id) }
                    .forEach { transaction ->
                        updateTransactionUseCase(
                            transaction.copy(category = category)
                        )
                    }

                clearSelection()
                loadTransactions()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "更新分类失败"
                    )
                }
            }
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                deleteTransactionUseCase(transactionId)
                loadTransactions()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
} 