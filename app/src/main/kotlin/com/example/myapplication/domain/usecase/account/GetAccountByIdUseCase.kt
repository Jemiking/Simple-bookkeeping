package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountByIdUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(id: Long): Result<Account?> {
        return try {
            val account = repository.getAccountById(id)
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 