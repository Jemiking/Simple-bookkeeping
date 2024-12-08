package com.example.myapplication.domain.usecase.account

import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(
        type: AccountType? = null,
        includeArchived: Boolean = false
    ): Flow<List<Account>> {
        return when {
            // 按类型查询活跃账户
            type != null -> {
                accountRepository.getAccountsByType(type)
            }
            // 查询已归档账户
            includeArchived -> {
                accountRepository.getAllArchivedAccounts()
            }
            // 查询所有活跃账户
            else -> {
                accountRepository.getAllActiveAccounts()
            }
        }.catch { e ->
            e.printStackTrace()
            emit(emptyList())
        }.map { accounts ->
            // 按排序索引排序
            accounts.sortedBy { it.orderIndex }
        }
    }
} 