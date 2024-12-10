package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.data.local.security.CryptoManager
import com.example.myapplication.domain.model.Transaction
import java.math.BigDecimal
import javax.inject.Inject

class TransactionMapper @Inject constructor(
    private val cryptoManager: CryptoManager
) {
    fun TransactionEntity.toDomain(
        categoryName: String,
        categoryIcon: String,
        categoryColor: Int,
        accountName: String
    ): Transaction {
        // 解密金额
        val decryptedAmount = cryptoManager.decrypt(amount)
        val amountValue = BigDecimal(String(decryptedAmount))

        // 解密备注
        val decryptedNote = note?.let {
            String(cryptoManager.decrypt(it))
        }

        return Transaction(
            id = id,
            amount = amountValue.toDouble(),
            type = TransactionType.valueOf(type),
            categoryId = categoryId ?: 0,
            categoryName = categoryName,
            categoryIcon = categoryIcon,
            categoryColor = categoryColor,
            accountId = accountId,
            accountName = accountName,
            note = decryptedNote,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun Transaction.toEntity(): TransactionEntity {
        // 加密金额
        val encryptedAmount = cryptoManager.encrypt(
            BigDecimal(amount).toString().toByteArray()
        )

        // 加密备注
        val encryptedNote = note?.let {
            cryptoManager.encrypt(it.toByteArray())
        }

        return TransactionEntity(
            id = id,
            amount = encryptedAmount,
            type = type.name,
            categoryId = if (categoryId == 0L) null else categoryId,
            accountId = accountId,
            note = encryptedNote,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 