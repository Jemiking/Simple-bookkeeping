package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 1")
    suspend fun getDefaultCategories(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
} 