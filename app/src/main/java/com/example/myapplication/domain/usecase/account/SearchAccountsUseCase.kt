package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(query: String): Flow<List<Account>> {
        return repository.searchAccounts(query)
    }
} 