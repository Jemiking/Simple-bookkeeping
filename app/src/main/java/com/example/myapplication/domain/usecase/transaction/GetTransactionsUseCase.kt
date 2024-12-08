package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        type: TransactionType? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        categoryId: Long? = null,
        accountId: Long? = null
    ): Flow<List<Transaction>> {
        return when {
            // 按类型查询
            type != null -> {
                transactionRepository.getTransactionsByType(type)
            }
            // 按日期范围查询
            startDate != null && endDate != null -> {
                transactionRepository.getTransactionsByDateRange(startDate, endDate)
            }
            // 按分类查询
            categoryId != null -> {
                transactionRepository.getTransactionsByCategory(categoryId)
            }
            // 按账户查询
            accountId != null -> {
                transactionRepository.getTransactionsByAccount(accountId)
            }
            // 获取所有交易
            else -> {
                transactionRepository.getAllTransactions()
            }
        }.catch { e ->
            // 处理异常
            e.printStackTrace()
            emit(emptyList())
        }.map { transactions ->
            // 按日期降序排序
            transactions.sortedByDescending { it.date }
        }
    }
} 