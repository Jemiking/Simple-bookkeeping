package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Long> {
        return try {
            // 验证账户名称
            if (account.name.isBlank()) {
                return Result.error(IllegalArgumentException("账户名称不能为空"))
            }

            // 验证图标
            if (account.icon.isBlank()) {
                return Result.error(IllegalArgumentException("请选择账户图标"))
            }

            // 验证初始余额非负
            if (account.balance < 0) {
                return Result.error(IllegalArgumentException("初始余额不能为负数"))
            }

            // 如果是默认账户，需要检查是否已存在默认账户
            if (account.isDefault) {
                val existingDefault = accountRepository.getDefaultAccount()
                if (existingDefault != null) {
                    return Result.error(IllegalStateException("已存在默认账户：${existingDefault.name}"))
                }
            }

            // 检查同类型账户数量
            val sameTypeAccounts = accountRepository.getAccountsByType(account.type).first()
            if (sameTypeAccounts.size >= 10) {
                return Result.error(IllegalStateException("同类型账户不能超过10个"))
            }

            // 添加账户
            val id = accountRepository.addAccount(account)
            Result.success(id)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 