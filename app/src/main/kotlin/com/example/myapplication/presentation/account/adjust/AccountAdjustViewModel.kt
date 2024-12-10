package com.example.myapplication.presentation.account.adjust

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.account.AdjustAccountBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountAdjustViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val adjustAccountBalanceUseCase: AdjustAccountBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountAdjustState())
    val state: StateFlow<AccountAdjustState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAccountsUseCase().collect { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun onEvent(event: AccountAdjustEvent) {
        when (event) {
            is AccountAdjustEvent.SelectAccount -> {
                _state.update {
                    it.copy(
                        selectedAccount = event.account,
                        currentBalance = event.account.balance.toString(),
                        showAccountSelector = false
                    )
                }
            }
            is AccountAdjustEvent.UpdateNewBalance -> {
                val filteredBalance = event.balance.filter { it.isDigit() || it == '.' }
                _state.update { it.copy(newBalance = filteredBalance) }
            }
            is AccountAdjustEvent.UpdateNote -> {
                _state.update { it.copy(note = event.note) }
            }
            AccountAdjustEvent.ShowAccountSelector -> {
                _state.update { it.copy(showAccountSelector = true) }
            }
            AccountAdjustEvent.HideAccountSelector -> {
                _state.update { it.copy(showAccountSelector = false) }
            }
            AccountAdjustEvent.AdjustBalance -> {
                executeAdjustment()
            }
            AccountAdjustEvent.NavigateBack -> {
                // 导航逻辑将在UI层处理
            }
        }
    }

    private fun executeAdjustment() {
        val currentState = _state.value
        val account = currentState.selectedAccount
        val newBalance = currentState.newBalance.toDoubleOrNull()

        if (account == null) {
            _state.update { it.copy(error = "请选择账户") }
            return
        }

        if (newBalance == null) {
            _state.update { it.copy(error = "请输入有效的余额") }
            return
        }

        viewModelScope.launch {
            adjustAccountBalanceUseCase(
                accountId = account.id,
                newBalance = newBalance,
                note = currentState.note.takeIf { it.isNotBlank() }
            ).collect { result ->
                _state.update {
                    when (result) {
                        is com.example.myapplication.domain.util.Result.Success -> {
                            it.copy(
                                isLoading = false,
                                error = null
                            )
                        }
                        is com.example.myapplication.domain.util.Result.Error -> {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is com.example.myapplication.domain.util.Result.Loading -> {
                            it.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                    }
                }
            }
        }
    }
}