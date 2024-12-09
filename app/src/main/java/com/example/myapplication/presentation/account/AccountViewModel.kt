package com.example.myapplication.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.usecase.account.DeleteAccountUseCase
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.usecase.account.SearchAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val searchAccountsUseCase: SearchAccountsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AccountEffect>()
    val effect: SharedFlow<AccountEffect> = _effect.asSharedFlow()

    private var currentQuery = ""
    private var currentType: AccountType? = null

    init {
        loadAccounts()
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.Refresh -> {
                loadAccounts()
            }
            is AccountEvent.DeleteAccount -> {
                deleteAccount(event.accountId)
            }
            is AccountEvent.Search -> {
                currentQuery = event.query
                searchAccounts()
            }
            is AccountEvent.Filter -> {
                currentType = event.type
                searchAccounts()
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getAccountsUseCase().collect { accounts ->
                    _state.update { it.copy(
                        accounts = accounts,
                        isLoading = false,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "加载账户失败"
                ) }
                _effect.emit(AccountEffect.ShowError(e.message ?: "加载账户失败"))
            }
        }
    }

    private fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            try {
                deleteAccountUseCase(accountId)
                _effect.emit(AccountEffect.ShowMessage("账户已删除"))
                // 重新加载账户列表
                searchAccounts()
            } catch (e: Exception) {
                _effect.emit(AccountEffect.ShowError(e.message ?: "删除账户失败"))
            }
        }
    }

    private fun searchAccounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                searchAccountsUseCase(
                    query = currentQuery,
                    type = currentType
                ).collect { accounts ->
                    _state.update { it.copy(
                        accounts = accounts,
                        isLoading = false,
                        error = null
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "搜索账户失败"
                ) }
                _effect.emit(AccountEffect.ShowError(e.message ?: "搜索账户失败"))
            }
        }
    }
}

data class AccountState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AccountEffect {
    data class ShowMessage(val message: String) : AccountEffect()
    data class ShowError(val message: String) : AccountEffect()
}

sealed class AccountEvent {
    object Refresh : AccountEvent()
    data class DeleteAccount(val accountId: Long) : AccountEvent()
    data class Search(val query: String) : AccountEvent()
    data class Filter(val type: AccountType?) : AccountEvent()
} 