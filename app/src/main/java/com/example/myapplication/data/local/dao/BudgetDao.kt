package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE year_month = :yearMonth ORDER BY amount DESC")
    fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND year_month = :yearMonth")
    fun getBudgetByCategoryAndMonth(categoryId: Long, yearMonth: YearMonth): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE category_id IS NULL AND year_month = :yearMonth")
    fun getOverallBudgetByMonth(yearMonth: YearMonth): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    @Query("""
        SELECT b.* FROM budgets b
        WHERE b.year_month = :yearMonth
        AND b.is_enabled = 1
        AND (
            SELECT SUM(CAST(t.amount AS REAL))
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.type = 'EXPENSE'
            AND (
                (b.category_id IS NULL) OR
                (b.category_id = c.id)
            )
            AND strftime('%Y-%m', t.date) = strftime('%Y-%m', :yearMonth)
        ) >= b.amount * COALESCE(b.notify_threshold, 1.0)
    """)
    fun getBudgetsExceedingThreshold(yearMonth: YearMonth): Flow<List<BudgetEntity>>

    @Query("""
        SELECT COALESCE(SUM(CAST(t.amount AS REAL)), 0)
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        WHERE t.type = 'EXPENSE'
        AND (
            (:categoryId IS NULL) OR
            (c.id = :categoryId)
        )
        AND strftime('%Y-%m', t.date) = strftime('%Y-%m', :yearMonth)
    """)
    fun getCurrentSpending(categoryId: Long?, yearMonth: YearMonth): Flow<Double>
} 