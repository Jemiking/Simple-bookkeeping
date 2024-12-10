package com.example.myapplication.presentation.transaction.detail

import com.example.myapplication.domain.model.Transaction

sealed class TransactionDetailEvent {
    data class LoadTransaction(val id: Long) : TransactionDetailEvent()
    data class UpdateTransaction(val transaction: Transaction) : TransactionDetailEvent()
    object DeleteTransaction : TransactionDetailEvent()
    object ToggleEditMode : TransactionDetailEvent()
    object ShowDeleteConfirmation : TransactionDetailEvent()
    object HideDeleteConfirmation : TransactionDetailEvent()
    object NavigateBack : TransactionDetailEvent()
} 