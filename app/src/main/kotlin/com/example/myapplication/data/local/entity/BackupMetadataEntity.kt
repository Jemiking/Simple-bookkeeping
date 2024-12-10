package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filename: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val size: Long,
    val version: String,
    val description: String? = null,
    val isAutoBackup: Boolean = false
) 