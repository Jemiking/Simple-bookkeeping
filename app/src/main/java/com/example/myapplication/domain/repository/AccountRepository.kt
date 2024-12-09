package com.example.myapplication.domain.repository

import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    
    suspend fun getAccountById(id: Long): Account?
    
    fun getAccountsByType(type: AccountType): Flow<List<Account>>
    
    fun getTotalBalance(): Flow<Double>
    
    fun getTotalBalanceByType(type: AccountType): Flow<Double>
    
    suspend fun insertAccount(account: Account): Long
    
    suspend fun insertAccounts(accounts: List<Account>)
    
    suspend fun updateAccount(account: Account)
    
    suspend fun updateBalance(id: Long, amount: Double)
    
    suspend fun deleteAccount(id: Long)
    
    suspend fun deleteAccounts(ids: List<Long>)
    
    fun searchAccounts(query: String): Flow<List<Account>>
    
    fun getAccountCount(): Flow<Int>
    
    fun getAccounts(): Flow<List<Account>>
    
    fun getAccountById(id: Long): Flow<Account>
    
    fun searchAccounts(query: String, type: AccountType? = null): Flow<List<Account>>
    
    suspend fun getAccountBalance(id: Long): Double
    
    suspend fun updateAccountBalance(id: Long, newBalance: Double)
    
    suspend fun transferMoney(fromAccountId: Long, toAccountId: Long, amount: Double)
} 