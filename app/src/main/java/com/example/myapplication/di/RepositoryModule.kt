package com.example.myapplication.di

import com.example.myapplication.data.repository.AccountRepositoryImpl
import com.example.myapplication.data.repository.BudgetRepositoryImpl
import com.example.myapplication.data.repository.CategoryRepositoryImpl
import com.example.myapplication.data.repository.TransactionRepositoryImpl
import com.example.myapplication.data.repository.UserRepositoryImpl
import com.example.myapplication.domain.repository.AccountRepository
import com.example.myapplication.domain.repository.BudgetRepository
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.repository.TransactionRepository
import com.example.myapplication.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        repositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        repositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        repositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        repositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repositoryImpl: UserRepositoryImpl
    ): UserRepository
} 