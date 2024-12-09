package com.example.myapplication.domain.model

import java.time.LocalDateTime

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val note: String,
    val categoryId: Long,
    val accountId: Long,
    val date: LocalDateTime,
    val type: TransactionType,
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val images: List<String> = emptyList()
)

enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
} 