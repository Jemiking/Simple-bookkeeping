package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.mapper.toTransaction
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.core.exception.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange("", "").map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return dao.getTransactionById(id)?.toTransaction()
    }

    override suspend fun createTransaction(transaction: Transaction): Result<Long> {
        return try {
            val result = dao.updateTransactionAndBalance(
                transaction = transaction.toEntity(),
                oldTransaction = null
            )
            Result.success(result)
        } catch (e: IllegalStateException) {
            Result.failure(AppException.ValidationException(e.message ?: "创建交易失败"))
        } catch (e: Exception) {
            Result.failure(AppException.DatabaseException("创建交易失败: ${e.message}"))
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val oldTransaction = dao.getTransactionById(transaction.id)
                ?: return Result.failure(AppException.ValidationException("交易不存在"))
                
            dao.updateTransactionAndBalance(
                transaction = transaction.toEntity(),
                oldTransaction = oldTransaction
            )
            Result.success(Unit)
        } catch (e: IllegalStateException) {
            Result.failure(AppException.ValidationException(e.message ?: "更新交易失败"))
        } catch (e: Exception) {
            Result.failure(AppException.DatabaseException("更新交易失败: ${e.message}"))
        }
    }

    override suspend fun deleteTransaction(id: Long): Result<Unit> {
        return try {
            val transaction = dao.getTransactionById(id)
                ?: return Result.failure(AppException.ValidationException("交易不存在"))
                
            dao.deleteTransactionAndUpdateBalance(transaction)
            Result.success(Unit)
        } catch (e: IllegalStateException) {
            Result.failure(AppException.ValidationException(e.message ?: "删除交易失败"))
        } catch (e: Exception) {
            Result.failure(AppException.DatabaseException("删除交易失败: ${e.message}"))
        }
    }

    override suspend fun deleteTransactionsByAccount(accountId: Long): Result<Unit> {
        return try {
            dao.deleteTransactionsByAccountAndUpdateBalance(accountId)
            Result.success(Unit)
        } catch (e: IllegalStateException) {
            Result.failure(AppException.ValidationException(e.message ?: "批量删除交易失败"))
        } catch (e: Exception) {
            Result.failure(AppException.DatabaseException("批量删除交易失败: ${e.message}"))
        }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return dao.searchTransactionsWithRelations(query).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByAccount(accountId).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByDateRange(
        startDate: String,
        endDate: String
    ): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override suspend fun getTransactionCount(
        startDate: String,
        endDate: String
    ): Int {
        return dao.getTransactionCount(startDate, endDate)
    }

    override suspend fun getTransactionSum(
        type: String,
        startDate: String,
        endDate: String
    ): Double {
        return dao.getTransactionSum(type, startDate, endDate) ?: 0.0
    }

    override suspend fun getTopCategories(
        type: String,
        startDate: String,
        endDate: String,
        limit: Int
    ): Map<Long, Double> {
        return dao.getTopCategories(type, startDate, endDate, limit)
    }

    override suspend fun getRecentTransactions(
        limit: Int,
        startDate: LocalDateTime
    ): Flow<List<Transaction>> {
        return dao.getRecentTransactions(limit, startDate.toString())
            .map { entities ->
                entities.map { it.toTransaction() }
            }
    }
} 