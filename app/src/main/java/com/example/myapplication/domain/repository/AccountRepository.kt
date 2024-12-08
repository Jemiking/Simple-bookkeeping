package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Account
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface AccountRepository {
    suspend fun insertAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(accountId: UUID)
    suspend fun getAccountById(accountId: UUID): Account?
    fun getAllAccounts(): Flow<List<Account>>
    fun getActiveAccounts(): Flow<List<Account>>
    fun getArchivedAccounts(): Flow<List<Account>>
    suspend fun getDefaultAccount(): Account?
    suspend fun getAccountCount(): Int
    suspend fun archiveAccount(accountId: UUID)
    suspend fun unarchiveAccount(accountId: UUID)
    fun getAllArchivedAccounts(): Flow<List<Account>>
    fun getAllActiveAccounts(): Flow<List<Account>>
    fun getAccountsByType(type: AccountType): Flow<List<Account>>
} 