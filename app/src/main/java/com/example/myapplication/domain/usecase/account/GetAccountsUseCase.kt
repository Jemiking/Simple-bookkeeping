package com.example.myapplication.domain.usecase.account

import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(type: AccountType? = null): Flow<List<Account>> {
        return if (type != null) {
            repository.getAccountsByType(type)
        } else {
            repository.getAllAccounts()
        }
    }
} 