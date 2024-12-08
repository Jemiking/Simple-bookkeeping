package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Budget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<Budget>>
    
    fun getBudgetByCategoryAndMonth(
        categoryId: Long,
        yearMonth: YearMonth
    ): Flow<Budget?>
    
    fun getOverallBudgetByMonth(yearMonth: YearMonth): Flow<Budget?>
    
    suspend fun addBudget(budget: Budget): Long
    
    suspend fun updateBudget(budget: Budget)
    
    suspend fun deleteBudget(budget: Budget)
    
    suspend fun deleteBudgetById(id: Long)
    
    fun getBudgetsExceedingThreshold(yearMonth: YearMonth): Flow<List<Budget>>
    
    fun getCurrentSpending(
        categoryId: Long?,
        yearMonth: YearMonth
    ): Flow<Double>
    
    fun getBudgetWithSpending(
        budgetId: Long,
        yearMonth: YearMonth
    ): Flow<Budget>
} 