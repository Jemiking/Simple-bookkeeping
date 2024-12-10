package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.dao.*
import com.example.myapplication.data.local.migration.DatabaseCallback
import com.example.myapplication.data.repository.*
import com.example.myapplication.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback
    ): AppDatabase {
        return AppDatabase.getInstance(context, callback)
    }

    @Provides
    @Singleton
    fun provideTransactionDao(
        database: AppDatabase
    ): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(
        database: AppDatabase
    ): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(
        database: AppDatabase
    ): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(
        database: AppDatabase
    ): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    @Singleton
    fun provideBackupMetadataDao(
        database: AppDatabase
    ): BackupMetadataDao {
        return database.backupMetadataDao()
    }

    @Provides
    @Singleton
    fun provideBackupSettingsDao(
        database: AppDatabase
    ): BackupSettingsDao {
        return database.backupSettingsDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao
    ): TransactionRepository {
        return TransactionRepositoryImpl(transactionDao)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao
    ): AccountRepository {
        return AccountRepositoryImpl(accountDao)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao)
    }

    @Provides
    @Singleton
    fun provideBudgetRepository(
        budgetDao: BudgetDao
    ): BudgetRepository {
        return BudgetRepositoryImpl(budgetDao)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        backupMetadataDao: BackupMetadataDao,
        backupSettingsDao: BackupSettingsDao,
        dataRepository: DataRepository
    ): BackupRepository {
        return BackupRepositoryImpl(
            context,
            backupMetadataDao,
            backupSettingsDao,
            dataRepository
        )
    }

    @Provides
    @Singleton
    fun provideDataRepository(
        transactionDao: TransactionDao,
        accountDao: AccountDao,
        categoryDao: CategoryDao,
        budgetDao: BudgetDao
    ): DataRepository {
        return DataRepositoryImpl(
            transactionDao,
            accountDao,
            categoryDao,
            budgetDao
        )
    }

    @Provides
    @Singleton
    fun provideStatisticsRepository(
        transactionDao: TransactionDao,
        categoryDao: CategoryDao,
        budgetDao: BudgetDao
    ): StatisticsRepository {
        return StatisticsRepositoryImpl(
            transactionDao,
            categoryDao,
            budgetDao
        )
    }

    @Provides
    @Singleton
    fun provideDatabaseCallback(
        accountDao: AccountDao,
        categoryDao: CategoryDao
    ): DatabaseCallback {
        return DatabaseCallback(accountDao, categoryDao)
    }
} 