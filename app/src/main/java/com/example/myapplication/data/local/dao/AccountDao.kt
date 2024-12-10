package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isArchived = 1 ORDER BY name ASC")
    fun getArchivedAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE name LIKE '%' || :query || '%' AND isArchived = 0 ORDER BY name ASC")
    fun searchAccounts(query: String): Flow<List<AccountEntity>>

    @Query("SELECT SUM(balance) FROM accounts WHERE isArchived = 0")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT * FROM accounts WHERE type = :type AND isArchived = 0 ORDER BY name ASC")
    fun getAccountsByType(type: String): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()

    @Query("SELECT balance FROM accounts WHERE id = :id")
    suspend fun getAccountBalance(id: Long): Double

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateAccountBalance(id: Long, newBalance: Double)

    @Query("UPDATE accounts SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean)

    @Query("""
        SELECT CASE 
            WHEN EXISTS (
                SELECT 1 FROM accounts 
                WHERE id = :id AND balance >= :amount
            )
            THEN 1
            ELSE 0
        END
    """)
    suspend fun hasEnoughBalance(id: Long, amount: Double): Boolean

    @Transaction
    suspend fun validateAndUpdateBalance(id: Long, amount: Double, isDecrease: Boolean) {
        val currentBalance = getAccountBalance(id)
        if (isDecrease && currentBalance < amount) {
            throw IllegalStateException("账户余额不足")
        }
        
        val newBalance = if (isDecrease) {
            currentBalance - amount
        } else {
            currentBalance + amount
        }
        
        updateAccountBalance(id, newBalance)
    }

    @Transaction
    suspend fun transfer(fromAccountId: Long, toAccountId: Long, amount: Double) {
        // 1. 检查账户是否存在
        val fromAccount = getAccountById(fromAccountId) 
            ?: throw IllegalStateException("转出账户不存在")
        val toAccount = getAccountById(toAccountId)
            ?: throw IllegalStateException("转入账户不存在")
            
        // 2. 检查余额是否充足
        if (!hasEnoughBalance(fromAccountId, amount)) {
            throw IllegalStateException("转出账户余额不足")
        }
        
        // 3. 执行转账
        validateAndUpdateBalance(fromAccountId, amount, true)
        validateAndUpdateBalance(toAccountId, amount, false)
    }
} 