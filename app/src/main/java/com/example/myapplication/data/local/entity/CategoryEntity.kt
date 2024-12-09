package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDateTime

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: CategoryType,
    
    @ColumnInfo(name = "icon")
    val icon: String,
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,
    
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0,
    
    @ColumnInfo(name = "budget_amount")
    val budgetAmount: Double? = null,
    
    @ColumnInfo(name = "note")
    val note: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

enum class CategoryType {
    EXPENSE,    // 支出
    INCOME      // 收入
} 