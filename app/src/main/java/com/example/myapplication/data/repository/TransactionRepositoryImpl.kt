package com.example.myapplication.data.repository

import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) : TransactionRepository {

    override suspend fun createTransaction(transaction: Transaction): Long {
        return database.withTransaction {
            // 1. 插入交易记录
            val transactionId = transactionDao.insert(transaction.toEntity())
            
            // 2. 更新账户余额
            val currentBalance = accountDao.getAccountBalance(transaction.accountId)
            val newBalance = when (transaction.type) {
                TransactionType.INCOME -> currentBalance + transaction.amount
                TransactionType.EXPENSE -> currentBalance - transaction.amount
            }
            accountDao.updateAccountBalance(transaction.accountId, newBalance)
            
            transactionId
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        database.withTransaction {
            // 1. 获取原交易记录
            val oldTransaction = transactionDao.getTransactionById(transaction.id)
                ?: throw IllegalStateException("交易记录不存在")
            
            // 2. 恢复原账户余额
            val oldBalance = accountDao.getAccountBalance(oldTransaction.accountId)
            val restoredBalance = when (oldTransaction.type) {
                TransactionType.INCOME -> oldBalance - oldTransaction.amount
                TransactionType.EXPENSE -> oldBalance + oldTransaction.amount
            }
            accountDao.updateAccountBalance(oldTransaction.accountId, restoredBalance)
            
            // 3. 更新交易记录
            transactionDao.update(transaction.toEntity())
            
            // 4. 更新新账户余额
            val currentBalance = accountDao.getAccountBalance(transaction.accountId)
            val newBalance = when (transaction.type) {
                TransactionType.INCOME -> currentBalance + transaction.amount
                TransactionType.EXPENSE -> currentBalance - transaction.amount
            }
            accountDao.updateAccountBalance(transaction.accountId, newBalance)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        database.withTransaction {
            // 1. 删除交易记录
            transactionDao.delete(transaction.toEntity())
            
            // 2. 更新账户余额
            val currentBalance = accountDao.getAccountBalance(transaction.accountId)
            val newBalance = when (transaction.type) {
                TransactionType.INCOME -> currentBalance - transaction.amount
                TransactionType.EXPENSE -> currentBalance + transaction.amount
            }
            accountDao.updateAccountBalance(transaction.accountId, newBalance)
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAccount(accountId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(
            startDate.toString(),
            endDate.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            note = note,
            categoryId = categoryId,
            accountId = accountId,
            date = date,
            type = type.name,
            tags = tags.joinToString(","),
            location = location,
            images = images.joinToString(",")
        )
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            note = note,
            categoryId = categoryId,
            accountId = accountId,
            date = date,
            type = TransactionType.valueOf(type),
            tags = tags.split(",").filter { it.isNotEmpty() },
            location = location,
            images = images.split(",").filter { it.isNotEmpty() }
        )
    }
} 