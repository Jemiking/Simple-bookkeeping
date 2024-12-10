package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BackupMetadataEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BackupMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: BackupMetadataEntity): Long

    @Update
    suspend fun update(metadata: BackupMetadataEntity)

    @Delete
    suspend fun delete(metadata: BackupMetadataEntity)

    @Query("SELECT * FROM backup_metadata WHERE id = :id")
    suspend fun getById(id: Long): BackupMetadataEntity?

    @Query("SELECT * FROM backup_metadata ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BackupMetadataEntity>>

    @Query("SELECT * FROM backup_metadata WHERE isAutoBackup = 1 ORDER BY createdAt DESC")
    fun getAutoBackups(): Flow<List<BackupMetadataEntity>>

    @Query("SELECT * FROM backup_metadata WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BackupMetadataEntity>>

    @Query("DELETE FROM backup_metadata WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM backup_metadata WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM backup_metadata WHERE id NOT IN (SELECT id FROM backup_metadata ORDER BY createdAt DESC LIMIT :keepCount)")
    suspend fun deleteOldBackups(keepCount: Int)

    @Query("SELECT COUNT(*) FROM backup_metadata")
    suspend fun getCount(): Int

    @Query("SELECT SUM(size) FROM backup_metadata")
    suspend fun getTotalSize(): Long?
} 