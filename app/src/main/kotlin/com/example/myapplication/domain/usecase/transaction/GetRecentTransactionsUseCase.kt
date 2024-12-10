package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        limit: Int = 20,
        startDate: LocalDateTime = LocalDateTime.now().minusDays(7)
    ): Flow<Result<List<Transaction>>> {
        return repository.getRecentTransactions(limit, startDate)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 