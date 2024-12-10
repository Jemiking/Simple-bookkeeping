package com.example.myapplication.presentation.account.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.account.DeleteAccountUseCase
import com.example.myapplication.domain.usecase.account.GetAccountUseCase
import com.example.myapplication.domain.usecase.transaction.GetAccountTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailState(
    val account: Account? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val getAccountTransactionsUseCase: GetAccountTransactionsUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle["accountId"])

    private val _state = MutableStateFlow(AccountDetailState())
    val state = _state.asStateFlow()

    init {
        loadAccount()
    }

    fun loadAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                getAccountUseCase(accountId)
                    .catch { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "加载账户详情失败"
                            )
                        }
                    }
                    .collect { account ->
                        _state.update {
                            it.copy(
                                account = account,
                                isLoading = false,
                                error = null
                            )
                        }
                        loadRecentTransactions()
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载账户详情失败"
                    )
                }
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            try {
                getAccountTransactionsUseCase(accountId, limit = 5)
                    .catch { e ->
                        _state.update {
                            it.copy(error = e.message ?: "加载最近交易失败")
                        }
                    }
                    .collect { transactions ->
                        _state.update {
                            it.copy(recentTransactions = transactions)
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "加载最近交易失败")
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                deleteAccountUseCase(accountId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "删除账户失败"
                    )
                }
            }
        }
    }
} 