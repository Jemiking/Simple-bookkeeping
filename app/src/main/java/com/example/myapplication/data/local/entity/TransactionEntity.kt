package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.Transaction
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.model.RecurringPeriod
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val accountId: String,
    val date: Date,
    val description: String,
    val tags: String, // 存储为JSON字符串
    val isRecurring: Boolean,
    val recurringPeriod: RecurringPeriod?
) {
    fun toDomainModel(): Transaction = Transaction(
        id = UUID.fromString(id),
        amount = BigDecimal(amount.toString()),
        type = type,
        categoryId = UUID.fromString(categoryId),
        accountId = UUID.fromString(accountId),
        date = date,
        description = description,
        tags = tags.split(",").filter { it.isNotEmpty() },
        isRecurring = isRecurring,
        recurringPeriod = recurringPeriod
    )

    companion object {
        fun fromDomainModel(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id.toString(),
            amount = transaction.amount.toDouble(),
            type = transaction.type,
            categoryId = transaction.categoryId.toString(),
            accountId = transaction.accountId.toString(),
            date = transaction.date,
            description = transaction.description,
            tags = transaction.tags.joinToString(","),
            isRecurring = transaction.isRecurring,
            recurringPeriod = transaction.recurringPeriod
        )
    }
} 