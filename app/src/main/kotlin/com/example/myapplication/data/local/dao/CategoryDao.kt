package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY name ASC")
    fun getByParentId(parentId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDefault = 1")
    suspend fun getDefaults(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Transaction
    suspend fun insertWithTransaction(category: CategoryEntity): Long {
        return insert(category)
    }

    @Transaction
    suspend fun updateWithTransaction(category: CategoryEntity) {
        update(category)
    }

    @Transaction
    suspend fun deleteWithTransaction(category: CategoryEntity) {
        delete(category)
    }
} 