package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.presentation.transaction.search.TransactionFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(
        query: String,
        filter: TransactionFilter = TransactionFilter()
    ): Flow<List<Transaction>> {
        return repository.searchTransactions(query)
            .map { transactions ->
                transactions
                    .filter { transaction ->
                        // 类型过滤
                        (filter.types.isEmpty() || transaction.type in filter.types) &&
                        // 日期范围过滤
                        (filter.startDate == null || transaction.date >= filter.startDate) &&
                        (filter.endDate == null || transaction.date <= filter.endDate) &&
                        // 金额范围过滤
                        (filter.minAmount == null || transaction.amount >= filter.minAmount) &&
                        (filter.maxAmount == null || transaction.amount <= filter.maxAmount) &&
                        // 分类过滤
                        (filter.categoryIds.isEmpty() || transaction.categoryId in filter.categoryIds) &&
                        // 账户过滤
                        (filter.accountIds.isEmpty() || transaction.accountId in filter.accountIds) &&
                        // 标签过滤
                        (filter.hasTags == null || (filter.hasTags == transaction.tags.isNotEmpty())) &&
                        // 位置过滤
                        (filter.hasLocation == null || (filter.hasLocation == (transaction.location != null))) &&
                        // 图片过滤
                        (filter.hasImages == null || (filter.hasImages == transaction.images.isNotEmpty()))
                    }
            }
    }
} 