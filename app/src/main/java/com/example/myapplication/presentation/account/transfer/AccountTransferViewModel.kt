package com.example.myapplication.presentation.account.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.account.TransferBetweenAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountTransferViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val transferBetweenAccountsUseCase: TransferBetweenAccountsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountTransferState())
    val state: StateFlow<AccountTransferState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAccountsUseCase().collect { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun onEvent(event: AccountTransferEvent) {
        when (event) {
            is AccountTransferEvent.SelectFromAccount -> {
                _state.update {
                    it.copy(
                        fromAccount = event.account,
                        showAccountSelector = false
                    )
                }
            }
            is AccountTransferEvent.SelectToAccount -> {
                _state.update {
                    it.copy(
                        toAccount = event.account,
                        showAccountSelector = false
                    )
                }
            }
            is AccountTransferEvent.UpdateAmount -> {
                val filteredAmount = event.amount.filter { it.isDigit() || it == '.' }
                _state.update { it.copy(amount = filteredAmount) }
            }
            is AccountTransferEvent.UpdateNote -> {
                _state.update { it.copy(note = event.note) }
            }
            AccountTransferEvent.ShowAccountSelector -> {
                _state.update { it.copy(showAccountSelector = true) }
            }
            AccountTransferEvent.HideAccountSelector -> {
                _state.update { it.copy(showAccountSelector = false) }
            }
            AccountTransferEvent.ToggleAccountSelectorType -> {
                _state.update { it.copy(isSelectingFromAccount = !it.isSelectingFromAccount) }
            }
            AccountTransferEvent.Transfer -> {
                executeTransfer()
            }
            AccountTransferEvent.NavigateBack -> {
                // 导航逻辑将在UI层处理
            }
        }
    }

    private fun executeTransfer() {
        val currentState = _state.value
        val fromAccount = currentState.fromAccount
        val toAccount = currentState.toAccount
        val amount = currentState.amount.toDoubleOrNull()

        if (fromAccount == null || toAccount == null) {
            _state.update { it.copy(error = "请选择转出和转入账户") }
            return
        }

        if (amount == null || amount <= 0) {
            _state.update { it.copy(error = "请输入有效的转账金额") }
            return
        }

        if (fromAccount.id == toAccount.id) {
            _state.update { it.copy(error = "不能转账到同一账户") }
            return
        }

        viewModelScope.launch {
            transferBetweenAccountsUseCase(
                fromAccountId = fromAccount.id,
                toAccountId = toAccount.id,
                amount = amount,
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