package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.BudgetDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao
) : BudgetRepository {

    override fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(yearMonth).map { budgets ->
            budgets.map { budget ->
                getBudgetWithDetails(budget, yearMonth)
            }
        }
    }

    override fun getBudgetByCategoryAndMonth(
        categoryId: Long,
        yearMonth: YearMonth
    ): Flow<Budget?> {
        return budgetDao.getBudgetByCategoryAndMonth(categoryId, yearMonth).map { budget ->
            budget?.let { getBudgetWithDetails(it, yearMonth) }
        }
    }

    override fun getOverallBudgetByMonth(yearMonth: YearMonth): Flow<Budget?> {
        return budgetDao.getOverallBudgetByMonth(yearMonth).map { budget ->
            budget?.let { getBudgetWithDetails(it, yearMonth) }
        }
    }

    override suspend fun addBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    override suspend fun deleteBudgetById(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    override fun getBudgetsExceedingThreshold(yearMonth: YearMonth): Flow<List<Budget>> {
        return budgetDao.getBudgetsExceedingThreshold(yearMonth).map { budgets ->
            budgets.map { budget ->
                getBudgetWithDetails(budget, yearMonth)
            }
        }
    }

    override fun getCurrentSpending(categoryId: Long?, yearMonth: YearMonth): Flow<Double> {
        return budgetDao.getCurrentSpending(categoryId, yearMonth)
    }

    override fun getBudgetWithSpending(budgetId: Long, yearMonth: YearMonth): Flow<Budget> {
        val budget = budgetDao.getBudgetsByMonth(yearMonth).map { budgets ->
            budgets.find { it.id == budgetId }
                ?: throw IllegalStateException("Budget not found")
        }

        val spending = budget.map { it.categoryId }.map { categoryId ->
            budgetDao.getCurrentSpending(categoryId, yearMonth)
        }

        return combine(budget, spending) { budgetEntity, spendingFlow ->
            getBudgetWithDetails(
                budgetEntity,
                yearMonth,
                spendingFlow.map { it }.hashCode().toDouble()
            )
        }
    }

    private suspend fun getBudgetWithDetails(
        budget: com.example.myapplication.data.local.entity.BudgetEntity,
        yearMonth: YearMonth,
        currentSpending: Double? = null
    ): Budget {
        val category = budget.categoryId?.let { categoryDao.getCategoryById(it) }
        val spending = currentSpending
            ?: budgetDao.getCurrentSpending(budget.categoryId, yearMonth)
                .map { it }.hashCode().toDouble()

        return budget.toDomain(
            categoryName = category?.name,
            categoryIcon = category?.icon,
            categoryColor = category?.color,
            currentSpending = spending
        )
    }
} 