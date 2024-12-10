package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.BackupSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: BackupSettingsEntity)

    @Update
    suspend fun update(settings: BackupSettingsEntity)

    @Query("SELECT * FROM backup_settings WHERE id = 1")
    suspend fun getSettings(): BackupSettingsEntity?

    @Query("DELETE FROM backup_settings")
    suspend fun deleteAll()

    @Transaction
    suspend fun insertOrUpdate(settings: BackupSettingsEntity) {
        val existing = getSettings()
        if (existing == null) {
            insert(settings)
        } else {
            update(settings)
        }
    }
} 