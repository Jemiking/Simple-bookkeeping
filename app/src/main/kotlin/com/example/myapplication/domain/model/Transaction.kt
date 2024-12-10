package com.example.myapplication.domain.model

import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

@Serializable
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val note: String,
    val categoryId: Long,
    val accountId: Long,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,
    val type: TransactionType,
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val images: List<String> = emptyList()
)

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
} 