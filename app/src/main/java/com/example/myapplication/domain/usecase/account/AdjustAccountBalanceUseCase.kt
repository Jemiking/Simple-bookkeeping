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

class AdjustAccountBalanceUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        accountId: Long,
        newBalance: Double,
        note: String? = null
    ): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading())

            // 获取当前账户
            val account = accountRepository.getAccountById(accountId)
                ?: throw IllegalArgumentException("账户不存在")

            // 计算调整金额
            val adjustment = newBalance - account.balance

            // 创建调整交易记录
            val transaction = Transaction(
                amount = kotlin.math.abs(adjustment),
                type = if (adjustment >= 0) TransactionType.INCOME else TransactionType.EXPENSE,
                categoryId = 0L, // 使用特殊的系统分类ID表示余额调整
                categoryName = "余额调整",
                categoryIcon = "account_balance",
                categoryColor = 0xFF9E9E9E.toInt(), // 使用灰色
                accountId = accountId,
                accountName = account.name,
                note = note ?: "账户余额调整",
                date = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            // 添加交易记录
            transactionRepository.addTransaction(transaction)

            // 更新账户余额
            accountRepository.updateAccountBalance(accountId, newBalance)

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}