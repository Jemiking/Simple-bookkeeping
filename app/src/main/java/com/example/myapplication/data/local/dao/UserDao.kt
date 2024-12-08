package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("UPDATE users SET theme = :theme WHERE id = :userId")
    suspend fun updateTheme(userId: String, theme: String)

    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateLanguage(userId: String, language: String)

    @Query("UPDATE users SET preferredCurrency = :currency WHERE id = :userId")
    suspend fun updatePreferredCurrency(userId: String, currency: String)

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE id = :userId")
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: String, newPasswordHash: String)
} 