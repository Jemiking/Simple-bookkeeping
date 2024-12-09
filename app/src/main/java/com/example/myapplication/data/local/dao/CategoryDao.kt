package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("""
        SELECT * FROM categories 
        WHERE is_deleted = 0 
        AND parent_id IS NULL 
        ORDER BY type ASC, order_index ASC, name ASC
    """)
    fun getRootCategories(): Flow<List<CategoryEntity>>
    
    @Query("""
        SELECT * FROM categories 
        WHERE is_deleted = 0 
        AND parent_id = :parentId 
        ORDER BY order_index ASC, name ASC
    """)
    fun getSubCategories(parentId: Long): Flow<List<CategoryEntity>>
    
    @Query("""
        SELECT * FROM categories 
        WHERE is_deleted = 0 
        AND type = :type 
        AND parent_id IS NULL 
        ORDER BY order_index ASC, name ASC
    """)
    fun getCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :id AND is_deleted = 0")
    suspend fun getCategoryById(id: Long): CategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)
    
    @Update
    suspend fun update(category: CategoryEntity)
    
    @Query("UPDATE categories SET order_index = :orderIndex WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, orderIndex: Int)
    
    @Query("UPDATE categories SET budget_amount = :amount WHERE id = :id")
    suspend fun updateBudgetAmount(id: Long, amount: Double?)
    
    @Query("UPDATE categories SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)
    
    @Query("""
        UPDATE categories 
        SET is_deleted = 1 
        WHERE id = :id OR parent_id = :id
    """)
    suspend fun softDeleteWithSubCategories(id: Long)
    
    @Query("UPDATE categories SET is_deleted = 1 WHERE id IN (:ids)")
    suspend fun softDeleteAll(ids: List<Long>)
    
    @Delete
    suspend fun hardDelete(category: CategoryEntity)
    
    @Query("""
        SELECT * FROM categories 
        WHERE is_deleted = 0 
        AND (name LIKE '%' || :query || '%' 
        OR note LIKE '%' || :query || '%')
        ORDER BY type ASC, order_index ASC, name ASC
    """)
    fun searchCategories(query: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT COUNT(*) FROM categories WHERE is_deleted = 0")
    fun getCategoryCount(): Flow<Int>
    
    @Query("""
        SELECT COUNT(*) FROM categories 
        WHERE is_deleted = 0 
        AND type = :type
    """)
    fun getCategoryCountByType(type: CategoryType): Flow<Int>
} 