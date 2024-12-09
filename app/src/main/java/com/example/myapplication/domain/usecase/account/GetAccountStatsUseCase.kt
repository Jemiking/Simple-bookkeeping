package com.example.myapplication.domain.usecase.account

import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AccountStats(
    val totalBalance: Double,
    val balanceByType: Map<AccountType, Double>,
    val accountCount: Int
)

class GetAccountStatsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<AccountStats> {
        return combine(
            repository.getTotalBalance(),
            combine(
                repository.getTotalBalanceByType(AccountType.CASH),
                repository.getTotalBalanceByType(AccountType.BANK_CARD),
                repository.getTotalBalanceByType(AccountType.CREDIT_CARD),
                repository.getTotalBalanceByType(AccountType.ALIPAY),
                repository.getTotalBalanceByType(AccountType.WECHAT),
                repository.getTotalBalanceByType(AccountType.OTHER)
            ) { cash, bank, credit, alipay, wechat, other ->
                mapOf(
                    AccountType.CASH to cash,
                    AccountType.BANK_CARD to bank,
                    AccountType.CREDIT_CARD to credit,
                    AccountType.ALIPAY to alipay,
                    AccountType.WECHAT to wechat,
                    AccountType.OTHER to other
                )
            },
            repository.getAccountCount()
        ) { total, balanceByType, count ->
            AccountStats(
                totalBalance = total,
                balanceByType = balanceByType,
                accountCount = count
            )
        }
    }
} 