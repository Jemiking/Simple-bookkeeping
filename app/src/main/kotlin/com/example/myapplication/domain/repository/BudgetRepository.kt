package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.BudgetWithCategory
import com.example.myapplication.domain.model.BudgetProgress
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<Budget>>
    
    fun getBudgetsByCategory(categoryId: Long): Flow<List<Budget>>
    
    fun getBudget(yearMonth: YearMonth, categoryId: Long): Flow<Budget?>
    
    fun getBudgetsWithCategory(yearMonth: YearMonth): Flow<List<BudgetWithCategory>>
    
    fun getBudgetProgress(yearMonth: YearMonth): Flow<List<BudgetProgress>>
    
    suspend fun insertBudget(budget: Budget): Long
    
    suspend fun updateBudget(budget: Budget)
    
    suspend fun deleteBudget(budget: Budget)
    
    suspend fun deleteBudgetsByMonth(yearMonth: YearMonth)
    
    suspend fun deleteBudgetsByCategory(categoryId: Long)
} 