package com.example.myapplication.domain.model

import com.example.myapplication.data.local.entity.CategoryType
import java.time.YearMonth

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val yearMonth: YearMonth,
    val amount: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class BudgetWithCategory(
    val id: Long,
    val categoryId: Long,
    val yearMonth: YearMonth,
    val amount: Double,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val categoryName: String,
    val categoryType: CategoryType
)

data class BudgetProgress(
    val id: Long,
    val categoryId: Long,
    val yearMonth: YearMonth,
    val amount: Double,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Double
) 