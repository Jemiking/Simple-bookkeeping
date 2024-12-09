package com.example.myapplication.domain.model

import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.AccountType
import java.time.LocalDateTime

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val icon: String,
    val color: String,
    val note: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean = false
) {
    companion object {
        fun fromEntity(entity: AccountEntity): Account {
            return Account(
                id = entity.id,
                name = entity.name,
                type = entity.type,
                balance = entity.balance,
                icon = entity.icon,
                color = entity.color,
                note = entity.note,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isDeleted = entity.isDeleted
            )
        }
    }

    fun toEntity(): AccountEntity {
        return AccountEntity(
            id = id,
            name = name,
            type = type,
            balance = balance,
            icon = icon,
            color = color,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }
} 