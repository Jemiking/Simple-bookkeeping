package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TransactionRepository {
    suspend fun createTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: Long): Transaction?
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int, startDate: LocalDateTime): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>>
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    suspend fun getTransactionCount(): Int
    suspend fun getTotalAmount(): Double
} 