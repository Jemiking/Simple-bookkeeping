package com.example.myapplication.data.local.backup

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.local.security.CryptoManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BackupManager @Inject constructor(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {
    // 获取数据库文件
    private val databaseFile: File
        get() = context.getDatabasePath("app_database")

    // 获取备份目录
    private val backupDir: File
        get() = File(context.getExternalFilesDir(null), "backup").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    // 创建备份
    suspend fun createBackup(): Result<File> {
        return try {
            // 生成备份文件名
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val backupFile = File(backupDir, "backup_$timestamp.db")

            // 复制并加密数据库文件
            FileInputStream(databaseFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    val bytes = input.readBytes()
                    val encryptedBytes = cryptoManager.encrypt(bytes)
                    output.write(encryptedBytes)
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 从文件恢复备份
    suspend fun restoreBackup(uri: Uri): Result<Unit> {
        return try {
            // 读取并解密备份文件
            val inputStream = context.contentResolver.openInputStream(uri)
            val encryptedBytes = inputStream?.readBytes() ?: throw IllegalStateException("无法读取备份文件")
            val decryptedBytes = cryptoManager.decrypt(encryptedBytes)

            // 关闭数据库连接
            context.deleteDatabase("app_database")

            // 写入解密后的数据
            FileOutputStream(databaseFile).use { output ->
                output.write(decryptedBytes)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 获取所有备份文件
    fun getBackupFiles(): List<File> {
        return backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") && it.name.endsWith(".db") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    // 删除备份文件
    fun deleteBackup(file: File): Boolean {
        return file.delete()
    }

    // 清理所有备份
    fun clearBackups() {
        backupDir.listFiles()?.forEach { it.delete() }
    }
} 