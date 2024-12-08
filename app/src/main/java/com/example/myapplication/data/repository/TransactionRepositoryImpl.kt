package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(TransactionEntity.fromDomainModel(transaction))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(TransactionEntity.fromDomainModel(transaction))
    }

    override suspend fun deleteTransaction(transactionId: UUID) {
        transactionDao.deleteTransaction(transactionId.toString())
    }

    override suspend fun getTransactionById(transactionId: UUID): Transaction? {
        return transactionDao.getTransactionById(transactionId.toString())?.toDomainModel()
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionsByAccount(accountId: UUID): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByAccount(accountId.toString()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionsByCategory(categoryId: UUID): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(categoryId.toString()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getTransactionCount(): Int {
        return transactionDao.getTransactionCount()
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }
} 