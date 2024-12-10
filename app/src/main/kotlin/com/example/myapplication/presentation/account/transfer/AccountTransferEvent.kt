package com.example.myapplication.presentation.account.transfer

import com.example.myapplication.domain.model.Account

sealed class AccountTransferEvent {
    data class SelectFromAccount(val account: Account) : AccountTransferEvent()
    data class SelectToAccount(val account: Account) : AccountTransferEvent()
    data class UpdateAmount(val amount: String) : AccountTransferEvent()
    data class UpdateNote(val note: String) : AccountTransferEvent()
    object ShowAccountSelector : AccountTransferEvent()
    object HideAccountSelector : AccountTransferEvent()
    object ToggleAccountSelectorType : AccountTransferEvent()
    object Transfer : AccountTransferEvent()
    object NavigateBack : AccountTransferEvent()
} 