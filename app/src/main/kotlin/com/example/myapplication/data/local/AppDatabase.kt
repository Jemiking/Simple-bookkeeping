package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.converter.DateTimeConverter
import com.example.myapplication.data.local.converter.ListConverter
import com.example.myapplication.data.local.dao.*
import com.example.myapplication.data.local.entity.*

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        TagEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateTimeConverter::class,
    ListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
} 