package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

interface TransactionRepository {
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: UUID)
    suspend fun getTransactionById(transactionId: UUID): Transaction?
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: UUID): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: UUID): Flow<List<Transaction>>
    suspend fun getTransactionCount(): Int
    suspend fun deleteAllTransactions()
} 