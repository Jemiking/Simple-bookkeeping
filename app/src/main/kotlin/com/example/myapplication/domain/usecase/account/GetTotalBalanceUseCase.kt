package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTotalBalanceUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<Result<Double>> {
        return repository.getTotalBalance()
            .map { Result.success(it ?: 0.0) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 