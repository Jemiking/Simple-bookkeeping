package com.example.myapplication.domain.model

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val amount: BigDecimal,
    val type: TransactionType,
    val categoryId: UUID,
    val accountId: UUID,
    val date: Date,
    val description: String,
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class RecurringPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
} 