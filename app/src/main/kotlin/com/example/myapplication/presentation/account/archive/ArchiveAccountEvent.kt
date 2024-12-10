package com.example.myapplication.presentation.account.archive

import com.example.myapplication.domain.model.Account

sealed class ArchiveAccountEvent {
    data class ArchiveAccount(val account: Account) : ArchiveAccountEvent()
    data class UnarchiveAccount(val account: Account) : ArchiveAccountEvent()
    object DismissError : ArchiveAccountEvent()
} 