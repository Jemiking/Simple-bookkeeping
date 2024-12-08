package com.example.myapplication.presentation.account.archive

import com.example.myapplication.domain.model.Account

data class ArchiveAccountState(
    val activeAccounts: List<Account> = emptyList(),
    val archivedAccounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 