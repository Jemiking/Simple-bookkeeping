package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 1 ORDER BY name ASC")
    fun getArchivedAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(): AccountEntity?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int

    @Query("UPDATE accounts SET isArchived = 1 WHERE id = :accountId")
    suspend fun archiveAccount(accountId: String)

    @Query("UPDATE accounts SET isArchived = 0 WHERE id = :accountId")
    suspend fun unarchiveAccount(accountId: String)

    @Query("SELECT * FROM accounts WHERE type = :type AND isArchived = 0 ORDER BY name ASC")
    fun getAccountsByType(type: AccountType): Flow<List<AccountEntity>>
} 