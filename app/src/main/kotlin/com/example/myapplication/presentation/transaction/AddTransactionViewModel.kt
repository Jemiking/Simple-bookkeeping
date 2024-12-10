package com.example.myapplication.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.transaction.AddTransactionUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddTransactionState())
    val state = combine(
        _state,
        getCategories(),
        getAccounts()
    ) { state, categories, accounts ->
        state.copy(
            categories = categories,
            accounts = accounts,
            // 默认选择第一个账户
            selectedAccount = state.selectedAccount ?: accounts.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddTransactionState()
    )

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.AmountChanged -> {
                updateAmount(event.amount)
            }
            is AddTransactionEvent.TypeChanged -> {
                _state.value = _state.value.copy(
                    type = event.type,
                    selectedCategory = null // 切换类型时清空已选分类
                )
            }
            is AddTransactionEvent.CategorySelected -> {
                _state.value = _state.value.copy(
                    selectedCategory = event.category,
                    categoryError = null
                )
            }
            is AddTransactionEvent.AccountSelected -> {
                _state.value = _state.value.copy(
                    selectedAccount = event.account,
                    accountError = null,
                    isAccountPickerVisible = false
                )
            }
            is AddTransactionEvent.NoteChanged -> {
                _state.value = _state.value.copy(note = event.note)
            }
            is AddTransactionEvent.DateChanged -> {
                _state.value = _state.value.copy(
                    date = event.date,
                    isDatePickerVisible = false
                )
            }
            AddTransactionEvent.ToggleDatePicker -> {
                _state.value = _state.value.copy(
                    isDatePickerVisible = !_state.value.isDatePickerVisible
                )
            }
            AddTransactionEvent.ToggleAccountPicker -> {
                _state.value = _state.value.copy(
                    isAccountPickerVisible = !_state.value.isAccountPickerVisible
                )
            }
            AddTransactionEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
            AddTransactionEvent.SaveTransaction -> {
                saveTransaction()
            }
            is AddTransactionEvent.NumberPressed -> {
                handleNumberInput(event.number)
            }
            AddTransactionEvent.DecimalPressed -> {
                handleDecimalInput()
            }
            AddTransactionEvent.BackspacePressed -> {
                handleBackspace()
            }
            AddTransactionEvent.ClearPressed -> {
                _state.value = _state.value.copy(amount = "0")
            }
        }
    }

    private fun updateAmount(newAmount: String) {
        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.value = _state.value.copy(
                amount = newAmount,
                amountError = null
            )
        }
    }

    private fun handleNumberInput(number: Int) {
        val currentAmount = _state.value.amount
        val newAmount = when {
            currentAmount == "0" -> number.toString()
            currentAmount.contains(".") -> {
                val parts = currentAmount.split(".")
                if (parts[1].length < 2) currentAmount + number else currentAmount
            }
            else -> currentAmount + number
        }
        updateAmount(newAmount)
    }

    private fun handleDecimalInput() {
        val currentAmount = _state.value.amount
        if (!currentAmount.contains(".")) {
            updateAmount("$currentAmount.")
        }
    }

    private fun handleBackspace() {
        val currentAmount = _state.value.amount
        val newAmount = when {
            currentAmount.length <= 1 -> "0"
            else -> currentAmount.dropLast(1)
        }
        updateAmount(newAmount)
    }

    private fun saveTransaction() {
        val currentState = _state.value
        
        // 验证输入
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val amount = currentState.amount.toDoubleOrNull() ?: 0.0
            val transaction = Transaction(
                amount = amount,
                type = currentState.type,
                categoryId = currentState.selectedCategory?.id ?: 0,
                categoryName = currentState.selectedCategory?.name ?: "",
                categoryIcon = currentState.selectedCategory?.icon ?: "",
                categoryColor = currentState.selectedCategory?.color ?: 0,
                accountId = currentState.selectedAccount?.id ?: 0,
                accountName = currentState.selectedAccount?.name ?: "",
                note = currentState.note,
                date = currentState.date,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            when (val result = addTransaction(transaction)) {
                is Result.Success -> {
                    // 重置状态
                    _state.value = AddTransactionState(
                        type = currentState.type,
                        selectedAccount = currentState.selectedAccount
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value
        var isValid = true

        // 验证金额
        if (currentState.amount.toDoubleOrNull() == null || currentState.amount.toDouble() <= 0) {
            _state.value = currentState.copy(amountError = "请输入有效金额")
            isValid = false
        }

        // 验证分类
        if (currentState.selectedCategory == null) {
            _state.value = currentState.copy(categoryError = "请选择分类")
            isValid = false
        }

        // 验证账户
        if (currentState.selectedAccount == null) {
            _state.value = currentState.copy(accountError = "请选择账户")
            isValid = false
        }

        return isValid
    }
} 