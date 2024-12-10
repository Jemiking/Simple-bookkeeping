package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.domain.model.Account

fun AccountEntity.toDomain(
    transactionCount: Int = 0,
    monthlyIncome: Double = 0.0,
    monthlyExpense: Double = 0.0
): Account {
    return Account(
        id = id,
        name = name,
        type = type,
        balance = balance,
        icon = icon,
        color = color,
        orderIndex = orderIndex,
        isDefault = isDefault,
        isArchived = isArchived,
        transactionCount = transactionCount,
        monthlyIncome = monthlyIncome,
        monthlyExpense = monthlyExpense
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type,
        balance = balance,
        icon = icon,
        color = color,
        orderIndex = orderIndex,
        isDefault = isDefault,
        isArchived = isArchived
    )
} 