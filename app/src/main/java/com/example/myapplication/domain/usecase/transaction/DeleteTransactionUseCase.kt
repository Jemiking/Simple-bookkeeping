package com.example.myapplication.domain.usecase.transaction

import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(transaction: Transaction): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading())
            repository.deleteTransaction(transaction)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "删除交易失败"))
        }
    }
} 