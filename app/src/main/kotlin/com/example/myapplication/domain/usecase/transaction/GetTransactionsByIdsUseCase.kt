package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsByIdsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(ids: List<Long>): Flow<List<Transaction>> {
        return repository.getTransactionsByIds(ids)
    }
} 