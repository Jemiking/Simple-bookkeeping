package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.mapper.toTransaction
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactions().map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionById(id: Long): Flow<Transaction?> {
        return dao.getTransactionById(id).map { entity ->
            entity?.toTransaction()
        }
    }

    override fun getTransactionsByIds(ids: List<Long>): Flow<List<Transaction>> {
        return dao.getTransactionsByIds(ids).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long) {
        dao.deleteTransaction(id)
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return dao.searchTransactions(query).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByCategory(categoryId).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    override fun getTransactionStats(): Flow<Map<String, Double>> {
        return dao.getTransactionStats()
    }
} 