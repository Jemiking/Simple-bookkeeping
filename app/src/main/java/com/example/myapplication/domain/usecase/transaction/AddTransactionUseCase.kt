package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.domain.util.Result
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            // 更新账户余额
            when (transaction.type) {
                com.example.myapplication.data.local.entity.TransactionType.INCOME -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount
                    )
                }
                com.example.myapplication.data.local.entity.TransactionType.EXPENSE -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        -transaction.amount
                    )
                }
                com.example.myapplication.data.local.entity.TransactionType.TRANSFER -> {
                    // 转账交易需要处理两个账户
                    // TODO: 需要添加目标账户ID到Transaction模型中
                }
            }

            // 添加交易记录
            val id = transactionRepository.addTransaction(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 