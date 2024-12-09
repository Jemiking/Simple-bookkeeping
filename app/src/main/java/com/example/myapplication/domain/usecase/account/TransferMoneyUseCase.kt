package com.example.myapplication.domain.usecase.account

import com.example.myapplication.domain.repository.AccountRepository
import javax.inject.Inject

class TransferMoneyUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double
    ) {
        require(fromAccountId != toAccountId) { "不能转账到同一账户" }
        require(amount > 0) { "转账金额必须大于0" }

        repository.transferMoney(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount
        )
    }
} 