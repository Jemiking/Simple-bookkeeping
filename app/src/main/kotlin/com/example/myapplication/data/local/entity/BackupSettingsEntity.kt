package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_settings")
data class BackupSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // 只有一条记录
    val autoBackupEnabled: Boolean = false,
    val autoBackupInterval: Int = 7,
    val keepBackupCount: Int = 5,
    val backupLocation: String? = null,
    val includeImages: Boolean = true,
    val compressBackup: Boolean = true,
    val encryptBackup: Boolean = false,
    val encryptionKey: String? = null
) 