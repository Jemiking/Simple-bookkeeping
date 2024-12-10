package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAccountsByTypeUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(type: AccountType): Flow<Result<List<Account>>> {
        return repository.getAccountsByType(type)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 