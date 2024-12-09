package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRelatedTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        categoryId: Long,
        accountId: Long,
        excludeId: Long,
        limit: Int = 5
    ): Flow<List<Transaction>> {
        return combine(
            repository.getTransactionsByCategory(categoryId),
            repository.getTransactionsByAccount(accountId)
        ) { categoryTransactions, accountTransactions ->
            // 合并两个列表并去重
            (categoryTransactions + accountTransactions)
                .distinctBy { it.id }
                .filter { it.id != excludeId }
                .sortedByDescending { it.date }
                .take(limit)
        }
    }
} 