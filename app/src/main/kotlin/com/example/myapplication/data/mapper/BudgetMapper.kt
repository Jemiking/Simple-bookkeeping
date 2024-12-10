package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.BudgetEntity
import com.example.myapplication.domain.model.Budget

fun BudgetEntity.toDomain(
    categoryName: String? = null,
    categoryIcon: String? = null,
    categoryColor: Int? = null,
    currentSpending: Double = 0.0
): Budget {
    return Budget(
        id = id,
        amount = amount,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        yearMonth = yearMonth,
        notifyThreshold = notifyThreshold,
        isEnabled = isEnabled,
        currentSpending = currentSpending,
        remainingAmount = amount - currentSpending,
        spendingPercentage = if (amount > 0) (currentSpending / amount) * 100 else 0.0,
        isExceeded = currentSpending > amount
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        amount = amount,
        categoryId = categoryId,
        yearMonth = yearMonth,
        notifyThreshold = notifyThreshold,
        isEnabled = isEnabled
    )
} 