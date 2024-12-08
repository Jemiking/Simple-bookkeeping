package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.converter.Converters
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.dao.UserDao
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.TransactionEntity
import com.example.myapplication.data.local.entity.UserEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "app_database"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
} 