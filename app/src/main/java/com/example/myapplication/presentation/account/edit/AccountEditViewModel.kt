package com.example.myapplication.presentation.account.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.usecase.account.GetAccountUseCase
import com.example.myapplication.domain.usecase.account.SaveAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class AccountEditState(
    val isEditing: Boolean = false,
    val name: String = "",
    val nameError: String? = null,
    val type: AccountType = AccountType.CASH,
    val showTypeDropdown: Boolean = false,
    val balance: String = "",
    val balanceError: String? = null,
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && nameError == null && balanceError == null
}

@HiltViewModel
class AccountEditViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long? = savedStateHandle["accountId"]
    private val _state = MutableStateFlow(AccountEditState(isEditing = accountId != null))
    val state = _state.asStateFlow()

    init {
        accountId?.let { loadAccount(it) }
    }

    private fun loadAccount(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getAccountUseCase(id).collect { account ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = account.name,
                            type = account.type,
                            balance = account.balance.toString(),
                            note = account.note
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载账户失败"
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

    fun updateType(type: AccountType) {
        _state.update { it.copy(type = type) }
    }

    fun updateBalance(balance: String) {
        _state.update {
            it.copy(
                balance = balance,
                balanceError = validateBalance(balance)
            )
        }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun toggleTypeDropdown() {
        _state.update { it.copy(showTypeDropdown = !it.showTypeDropdown) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    fun saveAccount() {
        val currentState = state.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val account = Account(
                    id = accountId ?: 0,
                    name = currentState.name,
                    type = currentState.type,
                    balance = BigDecimal(currentState.balance),
                    note = currentState.note
                )
                saveAccountUseCase(account)
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
                        error = e.message ?: "保存账户失败"
                    )
                }
            }
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "账户名称不能为空"
            name.length > 20 -> "账户名称不能超过20个字符"
            else -> null
        }
    }

    private fun validateBalance(balance: String): String? {
        return try {
            BigDecimal(balance)
            null
        } catch (e: Exception) {
            "请输入有效的金额"
        }
    }
} 