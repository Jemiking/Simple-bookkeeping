package com.example.myapplication.presentation.transaction.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.usecase.transaction.CreateTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.myapplication.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.myapplication.domain.usecase.transaction.UpdateTransactionUseCase
import com.example.myapplication.presentation.utils.FormValidator
import com.example.myapplication.presentation.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionEditState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<TransactionEditEffect>()
    val effect = _effect.asSharedFlow()

    // 表单验证状态
    private val _validationState = MutableStateFlow(ValidationState())
    val validationState = _validationState.asStateFlow()

    // 实时验证
    private fun validateForm() {
        val amountValidation = FormValidator.validateAmount(_state.value.amount)
        val accountValidation = FormValidator.validateAccountSelection(
            _state.value.accountId,
            _state.value.toAccountId,
            _state.value.type == TransactionType.TRANSFER
        )
        val categoryValidation = FormValidator.validateCategorySelection(
            _state.value.categoryId,
            _state.value.type == TransactionType.TRANSFER
        )
        val noteValidation = FormValidator.validateNote(_state.value.note)

        _validationState.update { currentState ->
            currentState.copy(
                amountError = (amountValidation as? ValidationResult.Error)?.message,
                accountError = (accountValidation as? ValidationResult.Error)?.message,
                categoryError = (categoryValidation as? ValidationResult.Error)?.message,
                noteError = (noteValidation as? ValidationResult.Error)?.message,
                isValid = listOf(
                    amountValidation,
                    accountValidation,
                    categoryValidation,
                    noteValidation
                ).all { it is ValidationResult.Success }
            )
        }
    }

    fun onEvent(event: TransactionEditEvent) {
        when (event) {
            is TransactionEditEvent.AmountChanged -> {
                _state.update { it.copy(amount = event.amount) }
                validateForm()
            }
            is TransactionEditEvent.TypeChanged -> {
                _state.update { it.copy(type = event.type) }
                validateForm()
            }
            is TransactionEditEvent.AccountSelected -> {
                _state.update { it.copy(accountId = event.accountId) }
                validateForm()
            }
            is TransactionEditEvent.ToAccountSelected -> {
                _state.update { it.copy(toAccountId = event.accountId) }
                validateForm()
            }
            is TransactionEditEvent.CategorySelected -> {
                _state.update { it.copy(categoryId = event.categoryId) }
                validateForm()
            }
            is TransactionEditEvent.DateChanged -> {
                _state.update { it.copy(date = event.date) }
            }
            is TransactionEditEvent.NoteChanged -> {
                _state.update { it.copy(note = event.note) }
                validateForm()
            }
            TransactionEditEvent.Submit -> {
                submitTransaction()
            }
            TransactionEditEvent.Delete -> {
                deleteTransaction()
            }
            TransactionEditEvent.ShowDeleteConfirmation -> {
                _state.update { it.copy(showDeleteConfirmation = true) }
            }
            TransactionEditEvent.HideDeleteConfirmation -> {
                _state.update { it.copy(showDeleteConfirmation = false) }
            }
            TransactionEditEvent.ClearForm -> {
                viewModelScope.launch {
                    _state.update { it.copy(
                        amount = "",
                        note = "",
                        categoryId = null,
                        toAccountId = null
                    )}
                    _effect.emit(TransactionEditEffect.ShowClearAnimation)
                }
            }
            TransactionEditEvent.CancelEdit -> {
                viewModelScope.launch {
                    _effect.emit(TransactionEditEffect.ShowCancelAnimation)
                    delay(300) // 等待动画完成
                    _effect.emit(TransactionEditEffect.NavigateBack)
                }
            }
            TransactionEditEvent.QuickSave -> {
                viewModelScope.launch {
                    if (_validationState.value.isValid) {
                        _effect.emit(TransactionEditEffect.ShowSaveAnimation)
                        submitTransaction()
                    } else {
                        _effect.emit(TransactionEditEffect.ShowError("请检查输入内容"))
                    }
                }
            }
            TransactionEditEvent.ShowMoreOptions -> {
                _state.update { it.copy(showMoreOptions = event.show) }
            }
            TransactionEditEvent.ShowFieldMenu -> {
                _state.update { 
                    it.copy(
                        showFieldMenu = it.showFieldMenu + (event.field to event.show),
                        activeField = if (event.show) event.field else null
                    )
                }
                viewModelScope.launch {
                    if (event.show) {
                        _effect.emit(TransactionEditEffect.ShowFieldAnimation(event.field))
                    }
                }
            }
        }
    }

    private fun submitTransaction() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // 验证表单
                validateForm()
                if (!_validationState.value.isValid) {
                    _effect.emit(TransactionEditEffect.ShowError("请检查输入内容"))
                    return@launch
                }

                // 创建或更新交易
                if (_state.value.transactionId == null) {
                    createTransactionUseCase(
                        amount = _state.value.amount.toDoubleOrNull() ?: 0.0,
                        type = _state.value.type,
                        accountId = _state.value.accountId ?: return@launch,
                        toAccountId = _state.value.toAccountId,
                        categoryId = _state.value.categoryId,
                        date = _state.value.date,
                        note = _state.value.note
                    )
                } else {
                    updateTransactionUseCase(
                        id = _state.value.transactionId,
                        amount = _state.value.amount.toDoubleOrNull() ?: 0.0,
                        type = _state.value.type,
                        accountId = _state.value.accountId ?: return@launch,
                        toAccountId = _state.value.toAccountId,
                        categoryId = _state.value.categoryId,
                        date = _state.value.date,
                        note = _state.value.note
                    )
                }
                _effect.emit(TransactionEditEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(TransactionEditEffect.ShowError(e.message ?: "未知错误"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun deleteTransaction() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                _state.value.transactionId?.let { id ->
                    deleteTransactionUseCase(id)
                }
                _effect.emit(TransactionEditEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(TransactionEditEffect.ShowError(e.message ?: "未知错误"))
            } finally {
                _state.update { it.copy(isLoading = false, showDeleteConfirmation = false) }
            }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val transaction = getTransactionByIdUseCase(id)
                _state.update {
                    it.copy(
                        transactionId = transaction.id,
                        amount = transaction.amount.toString(),
                        type = transaction.type,
                        accountId = transaction.accountId,
                        toAccountId = transaction.toAccountId,
                        categoryId = transaction.categoryId,
                        date = transaction.date,
                        note = transaction.note ?: ""
                    )
                }
                validateForm()
            } catch (e: Exception) {
                _effect.emit(TransactionEditEffect.ShowError(e.message ?: "未知错误"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun clearField(field: String) {
        when (field) {
            "amount" -> _state.update { it.copy(amount = "") }
            "note" -> _state.update { it.copy(note = "") }
            "category" -> _state.update { it.copy(categoryId = null) }
            "account" -> _state.update { it.copy(accountId = null) }
            "toAccount" -> _state.update { it.copy(toAccountId = null) }
        }
    }

    private fun validateField(field: String): Boolean {
        return when (field) {
            "amount" -> _validationState.value.amountError == null
            "note" -> _validationState.value.noteError == null
            "category" -> _validationState.value.categoryError == null
            "account" -> _validationState.value.accountError == null
            else -> true
        }
    }
}

data class TransactionEditState(
    val transactionId: Long? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val date: LocalDateTime = LocalDateTime.now(),
    val note: String = "",
    val toAccountId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val showMoreOptions: Boolean = false,
    val activeField: String? = null,
    val showFieldMenu: Map<String, Boolean> = emptyMap(),
    val fieldAnimations: Map<String, Boolean> = emptyMap()
)

sealed class TransactionEditEvent {
    data class AmountChanged(val amount: String) : TransactionEditEvent()
    data class TypeChanged(val type: TransactionType) : TransactionEditEvent()
    data class AccountSelected(val accountId: Long) : TransactionEditEvent()
    data class CategorySelected(val categoryId: Long) : TransactionEditEvent()
    data class DateChanged(val date: LocalDateTime) : TransactionEditEvent()
    data class NoteChanged(val note: String) : TransactionEditEvent()
    data class ToAccountSelected(val accountId: Long) : TransactionEditEvent()
    object Submit : TransactionEditEvent()
    object ShowDeleteConfirmation : TransactionEditEvent()
    object HideDeleteConfirmation : TransactionEditEvent()
    object Delete : TransactionEditEvent()
    object ClearForm : TransactionEditEvent()
    object CancelEdit : TransactionEditEvent()
    object QuickSave : TransactionEditEvent()
    data class ShowMoreOptions(val show: Boolean) : TransactionEditEvent()
    data class ShowFieldMenu(val field: String, val show: Boolean) : TransactionEditEvent()
}

sealed class TransactionEditEffect {
    data class ShowError(val message: String) : TransactionEditEffect()
    object NavigateBack : TransactionEditEffect()
    object ShowSaveAnimation : TransactionEditEffect()
    object ShowCancelAnimation : TransactionEditEffect()
    object ShowClearAnimation : TransactionEditEffect()
    data class ShowFieldAnimation(val field: String) : TransactionEditEffect()
}

data class ValidationState(
    val amountError: String? = null,
    val accountError: String? = null,
    val categoryError: String? = null,
    val noteError: String? = null,
    val isValid: Boolean = false
) 