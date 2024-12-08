package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.DefaultData
import com.example.myapplication.data.local.converter.EncryptedConverter
import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.dao.BudgetDao
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.local.security.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEncryptedConverter(cryptoManager: CryptoManager): EncryptedConverter {
        return EncryptedConverter(cryptoManager)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        encryptedConverter: EncryptedConverter
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
        .addTypeConverter(encryptedConverter)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = provideAppDatabase(context, encryptedConverter)
                    // 添加默认支出分类
                    DefaultData.defaultExpenseCategories.forEach { category ->
                        database.categoryDao().insertCategory(category)
                    }
                    // 添加默认收入分类
                    DefaultData.defaultIncomeCategories.forEach { category ->
                        database.categoryDao().insertCategory(category)
                    }
                    // 添加默认账户
                    DefaultData.defaultAccounts.forEach { account ->
                        database.accountDao().insertAccount(account)
                    }
                }
            }
        })
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }
} 