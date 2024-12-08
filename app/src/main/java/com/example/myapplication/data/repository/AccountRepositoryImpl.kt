package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.AccountDao
import com.example.myapplication.data.local.entity.AccountEntity
import com.example.myapplication.domain.model.Account
import com.example.myapplication.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(AccountEntity.fromDomainModel(account))
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(AccountEntity.fromDomainModel(account))
    }

    override suspend fun deleteAccount(accountId: UUID) {
        accountDao.deleteAccount(accountId.toString())
    }

    override suspend fun getAccountById(accountId: UUID): Account? {
        return accountDao.getAccountById(accountId.toString())?.toDomainModel()
    }

    override fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveAccounts(): Flow<List<Account>> {
        return accountDao.getActiveAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getArchivedAccounts(): Flow<List<Account>> {
        return accountDao.getArchivedAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount()?.toDomainModel()
    }

    override suspend fun getAccountCount(): Int {
        return accountDao.getAccountCount()
    }

    override suspend fun archiveAccount(accountId: UUID) {
        accountDao.archiveAccount(accountId.toString())
    }

    override suspend fun unarchiveAccount(accountId: UUID) {
        accountDao.unarchiveAccount(accountId.toString())
    }

    override fun getAllArchivedAccounts(): Flow<List<Account>> {
        return accountDao.getArchivedAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllActiveAccounts(): Flow<List<Account>> {
        return accountDao.getActiveAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAccountsByType(type: AccountType): Flow<List<Account>> {
        return accountDao.getAccountsByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
} 