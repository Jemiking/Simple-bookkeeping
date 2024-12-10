package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Double)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getCount(): Int

    @Query("SELECT SUM(balance) FROM accounts")
    suspend fun getTotalBalance(): Double?

    @Transaction
    suspend fun insertWithTransaction(account: AccountEntity): Long {
        return insert(account)
    }

    @Transaction
    suspend fun updateWithTransaction(account: AccountEntity) {
        update(account)
    }

    @Transaction
    suspend fun deleteWithTransaction(account: AccountEntity) {
        delete(account)
    }
} 