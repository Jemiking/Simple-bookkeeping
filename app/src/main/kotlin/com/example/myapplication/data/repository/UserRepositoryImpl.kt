package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.UserDao
import com.example.myapplication.data.local.entity.UserEntity
import com.example.myapplication.domain.model.User
import com.example.myapplication.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun createUser(user: User) {
        userDao.insertUser(UserEntity.fromDomainModel(user))
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(UserEntity.fromDomainModel(user))
    }

    override suspend fun deleteUser(userId: UUID) {
        userDao.getUserById(userId.toString())?.let { userDao.deleteUser(it) }
    }

    override suspend fun getUserById(userId: UUID): User? {
        return userDao.getUserById(userId.toString())?.toDomainModel()
    }

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)?.toDomainModel()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomainModel()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }

    override suspend fun updateTheme(userId: UUID, theme: User.Theme) {
        userDao.updateTheme(userId.toString(), theme.name)
    }

    override suspend fun updateLanguage(userId: UUID, language: String) {
        userDao.updateLanguage(userId.toString(), language)
    }

    override suspend fun updatePreferredCurrency(userId: UUID, currency: String) {
        userDao.updatePreferredCurrency(userId.toString(), currency)
    }

    override suspend fun updateNotificationsEnabled(userId: UUID, enabled: Boolean) {
        userDao.updateNotificationsEnabled(userId.toString(), enabled)
    }

    override suspend fun updatePassword(userId: UUID, newPasswordHash: String) {
        userDao.updatePassword(userId.toString(), newPasswordHash)
    }

    override suspend fun validateCredentials(username: String, passwordHash: String): User? {
        val user = userDao.getUserByUsername(username)
        return if (user?.passwordHash == passwordHash) {
            user.toDomainModel()
        } else {
            null
        }
    }
} 