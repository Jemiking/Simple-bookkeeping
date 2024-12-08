package com.example.myapplication.presentation.account.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.usecase.account.ArchiveAccountUseCase
import com.example.myapplication.domain.usecase.account.GetAccountsUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveAccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val archiveAccountUseCase: ArchiveAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ArchiveAccountState())
    val state: StateFlow<ArchiveAccountState> = _state.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccountsUseCase(includeArchived = true).collect { accounts ->
                _state.update { currentState ->
                    currentState.copy(
                        activeAccounts = accounts.filter { !it.isArchived },
                        archivedAccounts = accounts.filter { it.isArchived }
                    )
                }
            }
        }
    }

    fun onEvent(event: ArchiveAccountEvent) {
        when (event) {
            is ArchiveAccountEvent.ArchiveAccount -> {
                archiveAccount(event.account)
            }
            is ArchiveAccountEvent.UnarchiveAccount -> {
                unarchiveAccount(event.account)
            }
            is ArchiveAccountEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun archiveAccount(account: Account) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = archiveAccountUseCase(account)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = null
                    ) }
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    ) }
                }
            }
        }
    }

    private fun unarchiveAccount(account: Account) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = archiveAccountUseCase.unarchive(account)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = null
                    ) }
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.message
                    ) }
                }
            }
        }
    }
} 