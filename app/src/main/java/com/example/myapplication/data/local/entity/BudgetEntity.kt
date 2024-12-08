package com.example.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.YearMonth

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Long?,  // null means overall budget
    
    @ColumnInfo(name = "year_month")
    val yearMonth: YearMonth,
    
    @ColumnInfo(name = "notify_threshold")
    val notifyThreshold: Double? = null,  // percentage threshold for notification
    
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true
) 