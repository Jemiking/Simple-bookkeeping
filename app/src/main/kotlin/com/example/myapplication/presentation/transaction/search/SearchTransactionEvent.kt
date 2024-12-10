package com.example.myapplication.presentation.transaction.search

import com.example.myapplication.data.local.entity.TransactionType
import java.time.LocalDateTime

sealed class SearchTransactionEvent {
    data class UpdateQuery(val query: String) : SearchTransactionEvent()
    data class SelectType(val type: TransactionType?) : SearchTransactionEvent()
    data class SelectCategory(val categoryId: Long?) : SearchTransactionEvent()
    data class SelectAccount(val accountId: Long?) : SearchTransactionEvent()
    data class SetDateRange(
        val startDate: LocalDateTime?,
        val endDate: LocalDateTime?
    ) : SearchTransactionEvent()
    data class SetAmountRange(
        val minAmount: Double?,
        val maxAmount: Double?
    ) : SearchTransactionEvent()
    object ToggleFilters : SearchTransactionEvent()
    object ClearFilters : SearchTransactionEvent()
    object NavigateBack : SearchTransactionEvent()
} 