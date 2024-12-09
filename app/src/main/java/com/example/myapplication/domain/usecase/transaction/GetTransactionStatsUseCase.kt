package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import javax.inject.Inject

data class TransactionStats(
    val income: Double,
    val expense: Double,
    val balance: Double,
    val transactionCount: Int
)

class GetTransactionStatsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<TransactionStats> {
        return combine(
            repository.getTransactionsTotalByTypeAndDateRange(
                type = TransactionType.INCOME,
                startDate = startDate,
                endDate = endDate
            ),
            repository.getTransactionsTotalByTypeAndDateRange(
                type = TransactionType.EXPENSE,
                startDate = startDate,
                endDate = endDate
            ),
            repository.getTransactionCount(startDate, endDate)
        ) { income, expense, count ->
            TransactionStats(
                income = income,
                expense = expense,
                balance = income - expense,
                transactionCount = count
            )
        }
    }
} 