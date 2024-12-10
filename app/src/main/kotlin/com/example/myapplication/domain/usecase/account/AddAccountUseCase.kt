package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import javax.inject.Inject

class AddAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Result<Long> {
        return try {
            val id = repository.insertAccount(account)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 