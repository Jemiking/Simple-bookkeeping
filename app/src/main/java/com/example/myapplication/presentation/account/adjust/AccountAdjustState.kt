package com.example.myapplication.presentation.account.adjust

import com.example.myapplication.domain.model.Account

data class AccountAdjustState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val currentBalance: String = "",
    val newBalance: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAccountSelector: Boolean = false
) 