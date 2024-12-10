package com.example.myapplication.domain.usecase.backup

import android.net.Uri
import com.example.myapplication.data.local.backup.BackupManager
import com.example.myapplication.domain.util.Result
import java.io.File
import javax.inject.Inject

class BackupDatabaseUseCase @Inject constructor(
    private val backupManager: BackupManager
) {
    // 创建备份
    suspend fun createBackup(): Result<File> {
        return try {
            backupManager.createBackup()
                .fold(
                    onSuccess = { Result.success(it) },
                    onFailure = { Result.error(it) }
                )
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 恢复备份
    suspend fun restoreBackup(uri: Uri): Result<Unit> {
        return try {
            backupManager.restoreBackup(uri)
                .fold(
                    onSuccess = { Result.success(Unit) },
                    onFailure = { Result.error(it) }
                )
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 获取备份列表
    fun getBackups(): Result<List<File>> {
        return try {
            val backups = backupManager.getBackupFiles()
            Result.success(backups)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 删除备份
    fun deleteBackup(file: File): Result<Boolean> {
        return try {
            val success = backupManager.deleteBackup(file)
            if (success) {
                Result.success(true)
            } else {
                Result.error(IllegalStateException("删除备份文件失败"))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    // 清理所有备份
    fun clearBackups(): Result<Unit> {
        return try {
            backupManager.clearBackups()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 