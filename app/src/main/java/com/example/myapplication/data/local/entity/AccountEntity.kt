package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.model.AccountType
import java.math.BigDecimal
import java.util.UUID

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String,
    val icon: String,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val excludeFromStats: Boolean
) {
    fun toDomainModel(): Account = Account(
        id = UUID.fromString(id),
        name = name,
        type = type,
        balance = BigDecimal(balance.toString()),
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        excludeFromStats = excludeFromStats
    )

    companion object {
        fun fromDomainModel(account: Account): AccountEntity = AccountEntity(
            id = account.id.toString(),
            name = account.name,
            type = account.type,
            balance = account.balance.toDouble(),
            currency = account.currency,
            icon = account.icon,
            color = account.color,
            isDefault = account.isDefault,
            isArchived = account.isArchived,
            excludeFromStats = account.excludeFromStats
        )
    }
} 