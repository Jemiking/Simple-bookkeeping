package com.example.myapplication.presentation.transaction.batch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionsByIdsUseCase
import com.example.myapplication.domain.usecase.transaction.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class BatchEditState(
    val selectedCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val showCategoryDropdown: Boolean = false,
    val adjustmentAmount: String = "",
    val adjustmentAmountError: String? = null,
    val adjustmentPercentage: String = "",
    val adjustmentPercentageError: String? = null,
    val adjustmentDays: String = "",
    val adjustmentDaysError: String? = null,
    val adjustmentMonths: String = "",
    val adjustmentMonthsError: String? = null,
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = (adjustmentAmount.isNotBlank() && adjustmentAmountError == null) ||
                (adjustmentPercentage.isNotBlank() && adjustmentPercentageError == null) ||
                (adjustmentDays.isNotBlank() && adjustmentDaysError == null) ||
                (adjustmentMonths.isNotBlank() && adjustmentMonthsError == null) ||
                selectedCategory != null ||
                note.isNotBlank()
}

@HiltViewModel
class BatchEditViewModel @Inject constructor(
    private val getTransactionsByIdsUseCase: GetTransactionsByIdsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selectedTransactionIds: List<Long> = checkNotNull(savedStateHandle["transactionIds"])
    private val transactions = mutableListOf<Transaction>()

    private val _state = MutableStateFlow(BatchEditState(selectedCount = selectedTransactionIds.size))
    val state = _state.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getTransactionsByIdsUseCase(selectedTransactionIds)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载交易记录失败"
                            )
                        }
                    }
                    .collect { loadedTransactions ->
                        transactions.clear()
                        transactions.addAll(loadedTransactions)
                        _state.update {
                            it.copy(
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

    fun toggleCategoryDropdown() {
        _state.update { it.copy(showCategoryDropdown = !it.showCategoryDropdown) }
    }

    fun selectCategory(category: Category) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun updateAdjustmentAmount(amount: String) {
        _state.update {
            it.copy(
                adjustmentAmount = amount,
                adjustmentAmountError = validateAmount(amount)
            )
        }
    }

    fun updateAdjustmentPercentage(percentage: String) {
        _state.update {
            it.copy(
                adjustmentPercentage = percentage,
                adjustmentPercentageError = validatePercentage(percentage)
            )
        }
    }

    fun updateAdjustmentDays(days: String) {
        _state.update {
            it.copy(
                adjustmentDays = days,
                adjustmentDaysError = validateDays(days)
            )
        }
    }

    fun updateAdjustmentMonths(months: String) {
        _state.update {
            it.copy(
                adjustmentMonths = months,
                adjustmentMonthsError = validateMonths(months)
            )
        }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val currentState = state.value
                transactions.forEach { transaction ->
                    var updatedTransaction = transaction

                    // 更新分类
                    currentState.selectedCategory?.let { category ->
                        updatedTransaction = updatedTransaction.copy(category = category)
                    }

                    // 更新金额
                    if (currentState.adjustmentAmount.isNotBlank()) {
                        val adjustment = BigDecimal(currentState.adjustmentAmount)
                        updatedTransaction = updatedTransaction.copy(
                            amount = updatedTransaction.amount + adjustment
                        )
                    } else if (currentState.adjustmentPercentage.isNotBlank()) {
                        val percentage = BigDecimal(currentState.adjustmentPercentage)
                        val adjustment = updatedTransaction.amount * (percentage / BigDecimal(100))
                        updatedTransaction = updatedTransaction.copy(
                            amount = updatedTransaction.amount + adjustment
                        )
                    }

                    // 更新日期
                    var updatedDate = updatedTransaction.date
                    if (currentState.adjustmentDays.isNotBlank()) {
                        val days = currentState.adjustmentDays.toInt()
                        updatedDate = updatedDate.plusDays(days.toLong())
                    }
                    if (currentState.adjustmentMonths.isNotBlank()) {
                        val months = currentState.adjustmentMonths.toInt()
                        updatedDate = updatedDate.plusMonths(months.toLong())
                    }
                    updatedTransaction = updatedTransaction.copy(date = updatedDate)

                    // 更新备注
                    if (currentState.note.isNotBlank()) {
                        updatedTransaction = updatedTransaction.copy(note = currentState.note)
                    }

                    updateTransactionUseCase(updatedTransaction)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "保存失败"
                    )
                }
            }
        }
    }

    private fun validateAmount(amount: String): String? {
        return if (amount.isBlank()) {
            null
        } else {
            try {
                BigDecimal(amount)
                null
            } catch (e: Exception) {
                "请输入有效的金额"
            }
        }
    }

    private fun validatePercentage(percentage: String): String? {
        return if (percentage.isBlank()) {
            null
        } else {
            try {
                val value = BigDecimal(percentage)
                if (value < BigDecimal(-100) || value > BigDecimal(100)) {
                    "百分比必须在-100到100之间"
                } else {
                    null
                }
            } catch (e: Exception) {
                "请输入有效的百分比"
            }
        }
    }

    private fun validateDays(days: String): String? {
        return if (days.isBlank()) {
            null
        } else {
            try {
                days.toInt()
                null
            } catch (e: Exception) {
                "请输入有效的天数"
            }
        }
    }

    private fun validateMonths(months: String): String? {
        return if (months.isBlank()) {
            null
        } else {
            try {
                months.toInt()
                null
            } catch (e: Exception) {
                "请输入有效的月数"
            }
        }
    }
} 