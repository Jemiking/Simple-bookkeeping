package com.example.myapplication.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.usecase.account.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val getAllAccountsUseCase: GetAllAccountsUseCase,
    private val getArchivedAccountsUseCase: GetArchivedAccountsUseCase,
    private val searchAccountsUseCase: SearchAccountsUseCase,
    private val getAccountsByTypeUseCase: GetAccountsByTypeUseCase,
    private val getTotalBalanceUseCase: GetTotalBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AccountEffect>()
    val effect: SharedFlow<AccountEffect> = _effect.asSharedFlow()

    init {
        loadAllAccounts()
        loadArchivedAccounts()
        loadTotalBalance()
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.CreateAccount -> createAccount(event.account)
            is AccountEvent.UpdateAccount -> updateAccount(event.account)
            is AccountEvent.DeleteAccount -> deleteAccount(event.account)
            is AccountEvent.LoadAccount -> loadAccount(event.id)
            is AccountEvent.LoadAllAccounts -> loadAllAccounts()
            is AccountEvent.LoadArchivedAccounts -> loadArchivedAccounts()
            is AccountEvent.SearchAccounts -> searchAccounts(event.query)
            is AccountEvent.FilterByType -> filterByType(event.type)
        }
    }

    private fun createAccount(account: Account) {
        viewModelScope.launch {
            createAccountUseCase(account)
                .onSuccess { id ->
                    _effect.emit(AccountEffect.AccountCreated(id))
                    loadAllAccounts()
                    loadTotalBalance()
                }
                .onFailure { error ->
                    _effect.emit(AccountEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun updateAccount(account: Account) {
        viewModelScope.launch {
            updateAccountUseCase(account)
                .onSuccess {
                    _effect.emit(AccountEffect.AccountUpdated)
                    loadAllAccounts()
                    loadTotalBalance()
                }
                .onFailure { error ->
                    _effect.emit(AccountEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun deleteAccount(account: Account) {
        viewModelScope.launch {
            deleteAccountUseCase(account)
                .onSuccess {
                    _effect.emit(AccountEffect.AccountDeleted)
                    loadAllAccounts()
                    loadTotalBalance()
                }
                .onFailure { error ->
                    _effect.emit(AccountEffect.Error(error.message ?: "Unknown error"))
                }
        }
    }

    private fun loadAccount(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAccountByIdUseCase(id)
                .onSuccess { account ->
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    ) }
                }
        }
    }

    private fun loadAllAccounts() {
        viewModelScope.launch {
            getAllAccountsUseCase()
                .collect { result ->
                    result
                        .onSuccess { accounts ->
                            _state.update { it.copy(
                                accounts = accounts,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun loadArchivedAccounts() {
        viewModelScope.launch {
            getArchivedAccountsUseCase()
                .collect { result ->
                    result
                        .onSuccess { accounts ->
                            _state.update { it.copy(
                                archivedAccounts = accounts,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun searchAccounts(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(searchQuery = query) }
            searchAccountsUseCase(query)
                .collect { result ->
                    result
                        .onSuccess { accounts ->
                            _state.update { it.copy(
                                accounts = accounts,
                                isLoading = false,
                                error = null
                            ) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(
                                isLoading = false,
                                error = error.message
                            ) }
                        }
                }
        }
    }

    private fun filterByType(type: AccountType?) {
        viewModelScope.launch {
            _state.update { it.copy(selectedType = type) }
            if (type != null) {
                getAccountsByTypeUseCase(type)
                    .collect { result ->
                        result
                            .onSuccess { accounts ->
                                _state.update { it.copy(
                                    accounts = accounts,
                                    isLoading = false,
                                    error = null
                                ) }
                            }
                            .onFailure { error ->
                                _state.update { it.copy(
                                    isLoading = false,
                                    error = error.message
                                ) }
                            }
                    }
            } else {
                loadAllAccounts()
            }
        }
    }

    private fun loadTotalBalance() {
        viewModelScope.launch {
            getTotalBalanceUseCase()
                .collect { result ->
                    result
                        .onSuccess { balance ->
                            _state.update { it.copy(totalBalance = balance) }
                        }
                        .onFailure { error ->
                            _effect.emit(AccountEffect.Error(error.message ?: "Unknown error"))
                        }
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