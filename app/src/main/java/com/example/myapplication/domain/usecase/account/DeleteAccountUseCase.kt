package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.repository.AccountRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.deleteAccount(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 