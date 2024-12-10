package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TransactionDao {
    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE accountId = :accountId
        ORDER BY date DESC
    """)
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("""
        SELECT * FROM transactions 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    suspend fun getRecentTransactions(limit: Int): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions 
        WHERE (title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%')
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        AND (:type IS NULL OR type = :type)
        AND (:accountId IS NULL OR accountId = :accountId)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY date DESC
    """)
    fun searchTransactions(
        query: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        type: String? = null,
        accountId: Long? = null,
        categoryId: Long? = null
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE 
            (t.title LIKE '%' || :query || '%' OR 
            t.note LIKE '%' || :query || '%' OR
            a.name LIKE '%' || :query || '%' OR
            c.name LIKE '%' || :query || '%')
        ORDER BY t.date DESC
    """)
    fun searchTransactionsWithRelations(query: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionCount(startDate: String, endDate: String): Int

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = :type AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTransactionSum(type: String, startDate: String, endDate: String): Double?

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE type = :type AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId 
        ORDER BY total DESC 
        LIMIT :limit
    """)
    suspend fun getTopCategories(type: String, startDate: String, endDate: String, limit: Int): Map<Long, Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Transaction
    @Query("""
        SELECT t.*, a.name as accountName, c.name as categoryName 
        FROM transactions t
        INNER JOIN accounts a ON t.accountId = a.id
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.date BETWEEN :startDate AND :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithDetails(startDate: String, endDate: String): Flow<List<TransactionWithDetails>>

    @Transaction
    suspend fun updateTransactionAndBalance(
        transaction: TransactionEntity,
        oldTransaction: TransactionEntity? = null
    ) {
        // 1. 验证账户是否存在
        val accountExists = getAccountExists(transaction.accountId)
        if (!accountExists) {
            throw IllegalStateException("账户不存在")
        }

        // 2. 如果是支出,验证余额是否充足
        if (transaction.type == "EXPENSE") {
            val currentBalance = getAccountBalance(transaction.accountId)
            if (currentBalance < transaction.amount) {
                throw IllegalStateException("账户余额不足")
            }
        }

        try {
            if (oldTransaction != null) {
                // 3. 恢复原账户余额
                val oldBalance = getAccountBalance(oldTransaction.accountId)
                val restoredBalance = when (oldTransaction.type) {
                    "INCOME" -> oldBalance - oldTransaction.amount
                    "EXPENSE" -> oldBalance + oldTransaction.amount
                    else -> oldBalance
                }
                updateAccountBalance(oldTransaction.accountId, restoredBalance)
            }

            // 4. 更新交易记录
            if (oldTransaction != null) {
                update(transaction)
            } else {
                insert(transaction)
            }

            // 5. 更新新账户余额
            val currentBalance = getAccountBalance(transaction.accountId)
            val newBalance = when (transaction.type) {
                "INCOME" -> currentBalance + transaction.amount
                "EXPENSE" -> currentBalance - transaction.amount
                else -> currentBalance
            }
            updateAccountBalance(transaction.accountId, newBalance)
        } catch (e: Exception) {
            // 发生异常时自动回滚事务
            throw IllegalStateException("交易更新失败: ${e.message}")
        }
    }

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE id = :accountId)")
    suspend fun getAccountExists(accountId: Long): Boolean

    @Transaction
    suspend fun deleteTransactionAndUpdateBalance(transaction: TransactionEntity) {
        try {
            // 1. 恢复账户余额
            val currentBalance = getAccountBalance(transaction.accountId)
            val restoredBalance = when (transaction.type) {
                "INCOME" -> currentBalance - transaction.amount
                "EXPENSE" -> currentBalance + transaction.amount
                else -> currentBalance
            }
            updateAccountBalance(transaction.accountId, restoredBalance)

            // 2. 删除交易记录
            delete(transaction)
        } catch (e: Exception) {
            throw IllegalStateException("删除交易失败: ${e.message}")
        }
    }

    @Transaction
    suspend fun deleteTransactionsByAccountAndUpdateBalance(accountId: Long) {
        try {
            // 1. 获取账户所有交易
            val transactions = getTransactionsByAccountSync(accountId)
            
            // 2. 计算余额调整
            var balanceAdjustment = 0.0
            transactions.forEach { transaction ->
                balanceAdjustment += when (transaction.type) {
                    "INCOME" -> -transaction.amount
                    "EXPENSE" -> transaction.amount
                    else -> 0.0
                }
            }

            // 3. 更新账户余额
            val currentBalance = getAccountBalance(accountId)
            updateAccountBalance(accountId, currentBalance + balanceAdjustment)

            // 4. 删除交易记录
            deleteTransactionsByAccount(accountId)
        } catch (e: Exception) {
            throw IllegalStateException("批量删除交易失败: ${e.message}")
        }
    }

    @Query("""
        SELECT * FROM transactions 
        WHERE accountId = :accountId
    """)
    suspend fun getTransactionsByAccountSync(accountId: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteTransactionsByAccount(accountId: Long)

    @Query("SELECT balance FROM accounts WHERE id = :accountId")
    suspend fun getAccountBalance(accountId: Long): Double

    @Query("UPDATE accounts SET balance = :balance WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: Long, balance: Double)

    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate 
        ORDER BY date DESC 
        LIMIT :limit
    """)
    fun getRecentTransactions(limit: Int, startDate: String): Flow<List<TransactionEntity>>
}

data class TransactionWithDetails(
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "accountName") val accountName: String,
    @ColumnInfo(name = "categoryName") val categoryName: String
) 