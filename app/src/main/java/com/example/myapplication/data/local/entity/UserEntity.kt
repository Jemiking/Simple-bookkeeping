package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.User
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val nickname: String?,
    val avatar: String?,
    val preferredCurrency: String,
    val language: String,
    val theme: User.Theme,
    val notificationsEnabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun toDomainModel(): User = User(
        id = UUID.fromString(id),
        username = username,
        email = email,
        passwordHash = passwordHash,
        nickname = nickname,
        avatar = avatar,
        preferredCurrency = preferredCurrency,
        language = language,
        theme = theme,
        notificationsEnabled = notificationsEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomainModel(user: User): UserEntity = UserEntity(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            passwordHash = user.passwordHash,
            nickname = user.nickname,
            avatar = user.avatar,
            preferredCurrency = user.preferredCurrency,
            language = user.language,
            theme = user.theme,
            notificationsEnabled = user.notificationsEnabled,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
} 