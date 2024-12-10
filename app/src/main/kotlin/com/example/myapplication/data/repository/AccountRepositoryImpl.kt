package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.entity.AccountType
import com.example.myapplication.data.mapper.toAccount
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts()
            .map { entities -> entities.map { Account.fromEntity(it) } }
    }

    override suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)?.let { Account.fromEntity(it) }
    }

    override fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return accountDao.getAccountsByType(type)
            .map { entities -> entities.map { Account.fromEntity(it) } }
    }

    override fun getTotalBalance(): Flow<Double> {
        return accountDao.getTotalBalance()
            .map { it ?: 0.0 }
    }

    override fun getTotalBalanceByType(type: AccountType): Flow<Double> {
        return accountDao.getTotalBalanceByType(type)
            .map { it ?: 0.0 }
    }

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insert(account.toEntity())
    }

    override suspend fun insertAccounts(accounts: List<Account>) {
        accountDao.insertAll(accounts.map { it.toEntity() })
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.update(account.toEntity())
    }

    override suspend fun updateBalance(id: Long, amount: Double) {
        accountDao.updateBalance(id, amount)
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.softDelete(id)
    }

    override suspend fun deleteAccounts(ids: List<Long>) {
        accountDao.softDeleteAll(ids)
    }

    override fun searchAccounts(query: String): Flow<List<Account>> {
        return accountDao.searchAccounts(query)
            .map { entities -> entities.map { Account.fromEntity(it) } }
    }

    override fun getAccountCount(): Flow<Int> {
        return accountDao.getAccountCount()
    }

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAccounts().map { entities ->
            entities.map { it.toAccount() }
        }
    }

    override fun getAccountById(id: Long): Flow<Account> {
        return accountDao.getAccountById(id).map { it.toAccount() }
    }

    override fun searchAccounts(query: String, type: AccountType?): Flow<List<Account>> {
        return accountDao.searchAccounts(query, type).map { entities ->
            entities.map { it.toAccount() }
        }
    }

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccount(id)
    }

    override suspend fun getAccountBalance(id: Long): Double {
        return accountDao.getAccountBalance(id)
    }

    override suspend fun updateAccountBalance(id: Long, newBalance: Double) {
        accountDao.updateAccountBalance(id, newBalance)
    }

    override suspend fun transferMoney(fromAccountId: Long, toAccountId: Long, amount: Double) {
        // 在事务中执行转账操作
        accountDao.transfer(fromAccountId, toAccountId, amount)
    }
} 