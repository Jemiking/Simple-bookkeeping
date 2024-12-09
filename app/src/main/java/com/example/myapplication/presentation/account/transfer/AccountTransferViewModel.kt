package com.example.myapplication.presentation.account.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.account.TransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class AccountTransferState(
    val accounts: List<Account> = emptyList(),
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val amount: String = "",
    val amountError: String? = null,
    val note: String = "",
    val showFromAccountDropdown: Boolean = false,
    val showToAccountDropdown: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val transferSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = fromAccount != null &&
                toAccount != null &&
                amount.isNotBlank() &&
                amountError == null &&
                amount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true &&
                fromAccount.balance >= amount.toBigDecimalOrNull()!!
}

@HiltViewModel
class AccountTransferViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val transferUseCase: TransferUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fromAccountId: Long? = savedStateHandle["fromAccountId"]
    private val _state = MutableStateFlow(AccountTransferState())
    val state = _state.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getAccountsUseCase()
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载账户列表失败"
                            )
                        }
                    }
                    .collect { accounts ->
                        _state.update {
                            it.copy(
                                accounts = accounts,
                                isLoading = false,
                                error = null
                            )
                        }
                        fromAccountId?.let { id ->
                            accounts.find { it.id == id }?.let { account ->
                                selectFromAccount(account)
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载账户列表失败"
                    )
                }
            }
        }
    }

    fun selectFromAccount(account: Account) {
        _state.update {
            it.copy(
                fromAccount = account,
                toAccount = if (it.toAccount?.id == account.id) null else it.toAccount
            )
        }
    }

    fun selectToAccount(account: Account) {
        _state.update { it.copy(toAccount = account) }
    }

    fun updateAmount(amount: String) {
        _state.update {
            it.copy(
                amount = amount,
                amountError = validateAmount(amount, it.fromAccount)
            )
        }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun toggleFromAccountDropdown() {
        _state.update { it.copy(showFromAccountDropdown = !it.showFromAccountDropdown) }
    }

    fun toggleToAccountDropdown() {
        _state.update { it.copy(showToAccountDropdown = !it.showToAccountDropdown) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun transfer() {
        val currentState = state.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                transferUseCase(
                    fromAccountId = currentState.fromAccount!!.id,
                    toAccountId = currentState.toAccount!!.id,
                    amount = BigDecimal(currentState.amount),
                    note = currentState.note
                )
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        transferSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "转账失败"
                    )
                }
            }
        }
    }

    private fun validateAmount(amount: String, fromAccount: Account?): String? {
        return try {
            val value = BigDecimal(amount)
            when {
                value <= BigDecimal.ZERO -> "转账金额必须大于0"
                fromAccount != null && value > fromAccount.balance -> "余额不足"
                else -> null
            }
        } catch (e: Exception) {
            "请输入有效的金额"
        }
    }
} 