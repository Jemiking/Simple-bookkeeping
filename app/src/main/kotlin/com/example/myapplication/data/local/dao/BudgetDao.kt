package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAll(): Flow<List<BudgetEntity>>

    @Query("""
        SELECT * FROM budgets 
        WHERE startDate <= :date AND endDate >= :date 
        ORDER BY startDate DESC
    """)
    fun getActive(date: LocalDateTime = LocalDateTime.now()): Flow<List<BudgetEntity>>

    @Query("""
        SELECT * FROM budgets 
        WHERE categoryId = :categoryId 
        AND startDate <= :date AND endDate >= :date 
        ORDER BY startDate DESC
    """)
    fun getByCategory(categoryId: Long, date: LocalDateTime = LocalDateTime.now()): Flow<List<BudgetEntity>>

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getCount(): Int

    @Transaction
    suspend fun insertWithTransaction(budget: BudgetEntity): Long {
        return insert(budget)
    }

    @Transaction
    suspend fun updateWithTransaction(budget: BudgetEntity) {
        update(budget)
    }

    @Transaction
    suspend fun deleteWithTransaction(budget: BudgetEntity) {
        delete(budget)
    }
} 