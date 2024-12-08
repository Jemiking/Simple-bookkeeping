package com.example.myapplication.data.local.converter

import androidx.room.TypeConverter
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.data.local.entity.TransactionType
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun fromYearMonth(value: String?): YearMonth? {
        return value?.let { YearMonth.parse(it, yearMonthFormatter) }
    }

    @TypeConverter
    fun yearMonthToString(yearMonth: YearMonth?): String? {
        return yearMonth?.format(yearMonthFormatter)
    }

    @TypeConverter
    fun fromTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }

    @TypeConverter
    fun transactionTypeToString(type: TransactionType?): String? {
        return type?.name
    }

    @TypeConverter
    fun fromAccountType(value: String?): AccountType? {
        return value?.let { AccountType.valueOf(it) }
    }

    @TypeConverter
    fun accountTypeToString(type: AccountType?): String? {
        return type?.name
    }
} 