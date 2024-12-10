package com.example.myapplication.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val preferredCurrency: String = "CNY",
    val language: String = "zh",
    val theme: Theme = Theme.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    enum class Theme {
        LIGHT, DARK, SYSTEM
    }
} 