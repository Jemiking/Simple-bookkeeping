package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class TransactionStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactions: List<Transaction>,
    val categoryStats: List<CategoryStat>
)

data class CategoryStat(
    val categoryId: Long,
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

class GetTransactionStatsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        startDate: LocalDateTime = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()),
        endDate: LocalDateTime = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())
    ): Flow<TransactionStats> {
        val incomeFlow = transactionRepository.getTotalAmountByTypeAndDateRange(
            TransactionType.INCOME,
            startDate,
            endDate
        )

        val expenseFlow = transactionRepository.getTotalAmountByTypeAndDateRange(
            TransactionType.EXPENSE,
            startDate,
            endDate
        )

        val transactionsFlow = transactionRepository.getTransactionsByDateRange(
            startDate,
            endDate
        )

        return combine(
            incomeFlow,
            expenseFlow,
            transactionsFlow
        ) { income, expense, transactions ->
            val categoryStats = transactions
                .groupBy { it.categoryId }
                .map { (_, categoryTransactions) ->
                    val first = categoryTransactions.first()
                    val amount = categoryTransactions.sumOf { it.amount }
                    val total = when (first.type) {
                        TransactionType.INCOME -> income
                        TransactionType.EXPENSE -> expense
                        else -> 0.0
                    }
                    CategoryStat(
                        categoryId = first.categoryId,
                        categoryName = first.categoryName,
                        amount = amount,
                        percentage = if (total > 0) (amount / total) * 100 else 0.0,
                        transactionCount = categoryTransactions.size
                    )
                }
                .sortedByDescending { it.amount }

            TransactionStats(
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                transactions = transactions.sortedByDescending { it.date },
                categoryStats = categoryStats
            )
        }
    }
} 