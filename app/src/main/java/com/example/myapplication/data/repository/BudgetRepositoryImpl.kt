package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.BudgetDao
import com.example.myapplication.data.local.entity.BudgetEntity
import com.example.myapplication.domain.model.Budget
import com.example.myapplication.domain.model.BudgetWithCategory
import com.example.myapplication.domain.model.BudgetProgress
import com.example.myapplication.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(yearMonth).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBudgetsByCategory(categoryId: Long): Flow<List<Budget>> {
        return budgetDao.getBudgetsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getBudget(yearMonth: YearMonth, categoryId: Long): Flow<Budget?> {
        return budgetDao.getBudget(yearMonth, categoryId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getBudgetsWithCategory(yearMonth: YearMonth): Flow<List<BudgetWithCategory>> {
        return budgetDao.getBudgetsWithCategory(yearMonth).map { entities ->
            entities.map {
                BudgetWithCategory(
                    id = it.id,
                    categoryId = it.categoryId,
                    yearMonth = it.yearMonth,
                    amount = it.amount,
                    note = it.note,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    categoryName = it.categoryName,
                    categoryType = it.categoryType
                )
            }
        }
    }

    override fun getBudgetProgress(yearMonth: YearMonth): Flow<List<BudgetProgress>> {
        return budgetDao.getBudgetProgress(yearMonth).map { entities ->
            entities.map {
                BudgetProgress(
                    id = it.id,
                    categoryId = it.categoryId,
                    yearMonth = it.yearMonth,
                    amount = it.amount,
                    note = it.note,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    spentAmount = it.spentAmount,
                    remainingAmount = it.remainingAmount,
                    progress = it.progress
                )
            }
        }
    }

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    override suspend fun deleteBudgetsByMonth(yearMonth: YearMonth) {
        budgetDao.deleteBudgetsByMonth(yearMonth)
    }

    override suspend fun deleteBudgetsByCategory(categoryId: Long) {
        budgetDao.deleteBudgetsByCategory(categoryId)
    }

    private fun BudgetEntity.toDomain(): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            yearMonth = yearMonth,
            amount = amount,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Budget.toEntity(): BudgetEntity {
        return BudgetEntity(
            id = id,
            categoryId = categoryId,
            yearMonth = yearMonth,
            amount = amount,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 