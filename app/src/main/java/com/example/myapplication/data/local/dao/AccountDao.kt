package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.AccountType
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts")
    fun getAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<AccountEntity>

    @Query("""
        SELECT * FROM accounts 
        WHERE (:type IS NULL OR type = :type)
        AND (name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%')
    """)
    fun searchAccounts(query: String, type: AccountType?): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    fun getAccountCount(): Flow<Int>

    @Query("SELECT balance FROM accounts WHERE id = :id")
    suspend fun getAccountBalance(id: Long): Double

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateAccountBalance(id: Long, newBalance: Double)

    @Transaction
    suspend fun transfer(fromAccountId: Long, toAccountId: Long, amount: Double) {
        val fromBalance = getAccountBalance(fromAccountId)
        val toBalance = getAccountBalance(toAccountId)
        
        if (fromBalance < amount) {
            throw IllegalStateException("余额不足")
        }
        
        updateAccountBalance(fromAccountId, fromBalance - amount)
        updateAccountBalance(toAccountId, toBalance + amount)
    }
} 