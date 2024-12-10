package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE isDefault = 1")
    suspend fun getDefaults(): List<TagEntity>

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getCount(): Int

    @Transaction
    suspend fun insertWithTransaction(tag: TagEntity): Long {
        return insert(tag)
    }

    @Transaction
    suspend fun updateWithTransaction(tag: TagEntity) {
        update(tag)
    }

    @Transaction
    suspend fun deleteWithTransaction(tag: TagEntity) {
        delete(tag)
    }
} 