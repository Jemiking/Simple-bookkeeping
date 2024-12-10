package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.backup.BackupManager
import com.example.myapplication.data.local.security.CryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    
    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): BackupManager {
        return BackupManager(context, cryptoManager)
    }
} 