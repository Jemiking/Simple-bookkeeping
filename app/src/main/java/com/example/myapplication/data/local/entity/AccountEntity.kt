package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDateTime

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: AccountType,
    
    @ColumnInfo(name = "balance")
    val balance: Double,
    
    @ColumnInfo(name = "icon")
    val icon: String,
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "note")
    val note: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)

enum class AccountType {
    CASH,           // 现金
    BANK_CARD,      // 银行卡
    CREDIT_CARD,    // 信用卡
    ALIPAY,         // 支付宝
    WECHAT,         // 微信
    OTHER           // 其他
} 