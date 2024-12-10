package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ArchiveAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Unit> {
        return try {
            // 检查是否是默认账户
            if (account.isDefault) {
                return Result.error(IllegalStateException("默认账户不能归档"))
            }

            // 检查是否已归档
            if (account.isArchived) {
                return Result.error(IllegalStateException("账户已经归档"))
            }

            // 检查是否还有活跃账户
            val activeAccounts = accountRepository.getAllActiveAccounts().first()
            if (activeAccounts.size <= 1) {
                return Result.error(IllegalStateException("必须保留至少一个活跃账户"))
            }

            // 检查账户余额
            if (account.balance != 0.0) {
                return Result.error(IllegalStateException("只能归档余额为0的账户"))
            }

            // 归档账户
            accountRepository.archiveAccount(account.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    suspend fun unarchive(account: Account): Result<Unit> {
        return try {
            // 检查是否已归档
            if (!account.isArchived) {
                return Result.error(IllegalStateException("账户未归档"))
            }

            // 检查同类型账户数量
            val sameTypeAccounts = accountRepository.getAccountsByType(account.type).first()
            if (sameTypeAccounts.size >= 10) {
                return Result.error(IllegalStateException("同类型账户不能超过10个"))
            }

            // 取消归档
            accountRepository.unarchiveAccount(account.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 