package com.example.myapplication.domain.model

import java.time.YearMonth

data class Budget(
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: Int?,
    val yearMonth: YearMonth,
    val notifyThreshold: Double?,
    val isEnabled: Boolean = true,
    val currentSpending: Double = 0.0,
    val remainingAmount: Double = amount,
    val spendingPercentage: Double = 0.0,
    val isExceeded: Boolean = false
) 