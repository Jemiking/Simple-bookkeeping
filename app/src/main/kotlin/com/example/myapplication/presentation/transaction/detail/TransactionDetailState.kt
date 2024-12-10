package com.example.myapplication.presentation.transaction.detail

import com.example.myapplication.domain.model.Transaction

data class TransactionDetailState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val showDeleteConfirmation: Boolean = false
) 