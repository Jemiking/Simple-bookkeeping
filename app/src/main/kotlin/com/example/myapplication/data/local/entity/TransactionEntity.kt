package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.TransactionType
import java.time.LocalDateTime

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val note: String,
    val categoryId: Long,
    val accountId: Long,
    val date: LocalDateTime,
    val type: TransactionType,
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val images: List<String> = emptyList()
) 