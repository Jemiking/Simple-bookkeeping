package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            val id = repository.insertTransaction(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 