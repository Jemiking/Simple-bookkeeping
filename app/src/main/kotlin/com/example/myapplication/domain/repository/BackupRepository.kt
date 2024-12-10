package com.example.myapplication.domain.repository

import android.net.Uri
import com.example.myapplication.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface BackupRepository {
    // 备份操作
    suspend fun createBackup(description: String? = null): Result<BackupResult>
    suspend fun restoreBackup(uri: Uri): Result<BackupResult>
    suspend fun deleteBackup(backupId: Long): Result<Unit>
    suspend fun deleteBackups(backupIds: List<Long>): Result<Unit>
    suspend fun deleteOldBackups(keepCount: Int): Result<Unit>

    // 备份元数据操作
    suspend fun getBackupMetadata(backupId: Long): BackupMetadata?
    fun getAllBackupMetadata(): Flow<List<BackupMetadata>>
    fun getAutoBackupMetadata(): Flow<List<BackupMetadata>>
    fun getBackupMetadataByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<BackupMetadata>>
    
    // 备份设置操作
    suspend fun getBackupSettings(): BackupSettings
    suspend fun updateBackupSettings(settings: BackupSettings)
    
    // 自动备份操作
    suspend fun performAutoBackup(): Result<BackupResult>
    suspend fun scheduleNextAutoBackup()
    suspend fun cancelScheduledAutoBackup()
    
    // 备份进度
    fun getBackupProgress(): Flow<BackupProgress>
    fun getRestoreProgress(): Flow<BackupProgress>
    
    // 备份验证
    suspend fun validateBackup(uri: Uri): Result<Boolean>
    suspend fun validateBackupLocation(): Result<Boolean>
    
    // 存储空间管理
    suspend fun getAvailableStorage(): Long
    suspend fun getBackupsSize(): Long
} 