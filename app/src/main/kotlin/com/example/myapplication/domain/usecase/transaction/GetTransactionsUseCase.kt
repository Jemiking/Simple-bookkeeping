package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        offset: Int = 0,
        limit: Int = 20
    ): Flow<List<Transaction>> {
        return repository.getTransactions()
            .map { transactions ->
                transactions
                    .sortedByDescending { it.date }
                    .drop(offset)
                    .take(limit)
            }
    }
} 