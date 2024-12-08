package com.example.myapplication.data.local.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.myapplication.data.local.security.CryptoManager
import java.math.BigDecimal
import javax.inject.Inject

@ProvidedTypeConverter
class EncryptedConverter @Inject constructor(
    private val cryptoManager: CryptoManager
) {
    @TypeConverter
    fun fromEncryptedAmount(encryptedAmount: ByteArray?): BigDecimal? {
        if (encryptedAmount == null) return null
        val decryptedBytes = cryptoManager.decrypt(encryptedAmount)
        return BigDecimal(String(decryptedBytes))
    }

    @TypeConverter
    fun toEncryptedAmount(amount: BigDecimal?): ByteArray? {
        if (amount == null) return null
        return cryptoManager.encrypt(amount.toString().toByteArray())
    }

    @TypeConverter
    fun fromEncryptedString(encryptedString: ByteArray?): String? {
        if (encryptedString == null) return null
        val decryptedBytes = cryptoManager.decrypt(encryptedString)
        return String(decryptedBytes)
    }

    @TypeConverter
    fun toEncryptedString(value: String?): ByteArray? {
        if (value == null) return null
        return cryptoManager.encrypt(value.toByteArray())
    }
} 