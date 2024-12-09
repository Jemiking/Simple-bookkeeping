package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    fun getBudgetsByMonth(yearMonth: YearMonth): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    fun getBudgetsByCategory(categoryId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth AND categoryId = :categoryId")
    fun getBudget(yearMonth: YearMonth, categoryId: Long): Flow<BudgetEntity?>

    @Query("""
        SELECT b.*, c.name as categoryName, c.type as categoryType
        FROM budgets b
        INNER JOIN categories c ON b.categoryId = c.id
        WHERE b.yearMonth = :yearMonth
    """)
    fun getBudgetsWithCategory(yearMonth: YearMonth): Flow<List<BudgetWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE yearMonth = :yearMonth")
    suspend fun deleteBudgetsByMonth(yearMonth: YearMonth)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteBudgetsByCategory(categoryId: Long)

    @Transaction
    @Query("""
        SELECT 
            b.*,
            COALESCE(SUM(t.amount), 0.0) as spentAmount
        FROM budgets b
        LEFT JOIN transactions t ON 
            b.categoryId = t.categoryId AND 
            strftime('%Y-%m', t.date) = strftime('%Y-%m', :yearMonth)
        WHERE b.yearMonth = :yearMonth
        GROUP BY b.id
    """)
    fun getBudgetProgress(yearMonth: YearMonth): Flow<List<BudgetProgress>>
}

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
    val spentAmount: Double
) {
    val remainingAmount: Double
        get() = amount - spentAmount

    val progress: Double
        get() = if (amount > 0) (spentAmount / amount) * 100 else 0.0
} 