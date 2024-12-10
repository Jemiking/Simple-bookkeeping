package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        accountId: Long,
        limit: Int? = null
    ): Flow<List<Transaction>> {
        return repository.getTransactionsByAccount(accountId, limit)
    }
} 