package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Unit> {
        return try {
            repository.deleteAccount(account)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 