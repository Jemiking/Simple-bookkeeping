package com.example.myapplication.domain.model

import java.math.BigDecimal
import java.util.UUID

data class Account(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val type: AccountType,
    val balance: BigDecimal,
    val currency: String,
    val icon: String,
    val color: Int,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val excludeFromStats: Boolean = false
)

enum class AccountType {
    CASH,
    BANK,
    CREDIT_CARD,
    INVESTMENT,
    SAVINGS,
    WALLET,
    OTHER
} 