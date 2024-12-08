package com.example.myapplication.presentation.account.transfer

import com.example.myapplication.domain.model.Account

data class AccountTransferState(
    val accounts: List<Account> = emptyList(),
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val amount: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAccountSelector: Boolean = false,
    val isSelectingFromAccount: Boolean = false
) 