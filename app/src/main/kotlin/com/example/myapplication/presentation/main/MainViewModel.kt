package com.example.myapplication.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.usecase.account.GetAllAccountsUseCase
import com.example.myapplication.domain.usecase.transaction.GetRecentTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllAccountsUseCase: GetAllAccountsUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect: SharedFlow<MainEffect> = _effect.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // 并行加载数据
                launch { loadAccounts() }
                launch { loadRecentTransactions() }
                
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _effect.emit(MainEffect.Error(e.message ?: "加载数据失败"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun loadAccounts() {
        getAllAccountsUseCase()
            .catch { e -> 
                _effect.emit(MainEffect.Error(e.message ?: "加载账户失败"))
            }
            .collect { result ->
                result.onSuccess { accounts ->
                    _state.update { it.copy(accounts = accounts) }
                }.onFailure { e ->
                    _effect.emit(MainEffect.Error(e.message ?: "加载账户失败"))
                }
            }
    }

    private suspend fun loadRecentTransactions() {
        getRecentTransactionsUseCase()
            .catch { e -> 
                _effect.emit(MainEffect.Error(e.message ?: "加载交易记录失败"))
            }
            .collect { result ->
                result.onSuccess { transactions ->
                    _state.update { it.copy(recentTransactions = transactions) }
                }.onFailure { e ->
                    _effect.emit(MainEffect.Error(e.message ?: "加载交易记录失败"))
                }
            }
    }

    fun onEvent(event: MainEvent) {
        viewModelScope.launch {
            when (event) {
                is MainEvent.RefreshData -> loadInitialData()
                is MainEvent.SelectAccount -> selectAccount(event.accountId)
                is MainEvent.SelectMonth -> selectMonth(event.month)
            }
        }
    }

    private fun selectAccount(accountId: Long) {
        _state.update { it.copy(selectedAccountId = accountId) }
    }

    private fun selectMonth(month: LocalDateTime) {
        _state.update { it.copy(selectedMonth = month) }
    }
}

data class MainState(
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedMonth: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MainEvent {
    object RefreshData : MainEvent()
    data class SelectAccount(val accountId: Long) : MainEvent()
    data class SelectMonth(val month: LocalDateTime) : MainEvent()
}

sealed class MainEffect {
    data class Error(val message: String) : MainEffect()
    object DataRefreshed : MainEffect()
} 