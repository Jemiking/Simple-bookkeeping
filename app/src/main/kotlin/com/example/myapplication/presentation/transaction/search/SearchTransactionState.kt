package com.example.myapplication.presentation.transaction.search

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import java.time.LocalDateTime

data class SearchTransactionState(
    val query: String = "",
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: TransactionType? = null,
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val showFilters: Boolean = false
) 