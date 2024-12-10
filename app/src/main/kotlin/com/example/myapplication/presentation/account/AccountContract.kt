package com.example.myapplication.presentation.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType

data class AccountState(
    val accounts: List<Account> = emptyList(),
    val archivedAccounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val selectedType: AccountType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AccountEvent {
    data class CreateAccount(val account: Account) : AccountEvent()
    data class UpdateAccount(val account: Account) : AccountEvent()
    data class DeleteAccount(val account: Account) : AccountEvent()
    data class LoadAccount(val id: Long) : AccountEvent()
    object LoadAllAccounts : AccountEvent()
    object LoadArchivedAccounts : AccountEvent()
    data class SearchAccounts(val query: String) : AccountEvent()
    data class FilterByType(val type: AccountType?) : AccountEvent()
}

sealed class AccountEffect {
    data class AccountCreated(val id: Long) : AccountEffect()
    object AccountUpdated : AccountEffect()
    object AccountDeleted : AccountEffect()
    data class Error(val message: String) : AccountEffect()
} 