package com.example.myapplication.domain.usecase.account

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Inject

class TransferBetweenAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String? = null
    ): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading())

            // 获取账户信息
            val fromAccount = accountRepository.getAccountById(fromAccountId)
            val toAccount = accountRepository.getAccountById(toAccountId)

            if (fromAccount == null || toAccount == null) {
                emit(Result.Error("账户不存在"))
                return@flow
            }

            if (amount <= 0) {
                emit(Result.Error("转账金额必须大于0"))
                return@flow
            }

            if (fromAccount.balance < amount) {
                emit(Result.Error("账户余额不足"))
                return@flow
            }

            // 创建转出交易
            val transferOutTransaction = Transaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                categoryId = -1, // 使用特殊的转账分类ID
                categoryName = "转账支出",
                accountId = fromAccountId,
                accountName = fromAccount.name,
                date = LocalDateTime.now(),
                note = note ?: "转账至${toAccount.name}"
            )

            // 创建转入交易
            val transferInTransaction = Transaction(
                amount = amount,
                type = TransactionType.INCOME,
                categoryId = -1, // 使用特殊的转账分类ID
                categoryName = "转账收入",
                accountId = toAccountId,
                accountName = toAccount.name,
                date = LocalDateTime.now(),
                note = note ?: "来自${fromAccount.name}的转账"
            )

            // 执行转账操作
            accountRepository.transferBetweenAccounts(fromAccountId, toAccountId, amount)

            // 记录转账交易
            transactionRepository.addTransaction(transferOutTransaction)
            transactionRepository.addTransaction(transferInTransaction)

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "转账失败"))
        }
    }
} 