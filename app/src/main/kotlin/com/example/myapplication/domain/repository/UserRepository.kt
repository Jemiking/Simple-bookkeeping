package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface UserRepository {
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: UUID)
    suspend fun getUserById(userId: UUID): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserCount(): Int
    suspend fun updateTheme(userId: UUID, theme: User.Theme)
    suspend fun updateLanguage(userId: UUID, language: String)
    suspend fun updatePreferredCurrency(userId: UUID, currency: String)
    suspend fun updateNotificationsEnabled(userId: UUID, enabled: Boolean)
    suspend fun updatePassword(userId: UUID, newPasswordHash: String)
    suspend fun validateCredentials(username: String, passwordHash: String): User?
} 