package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.SearchHistoryDao
import com.example.myapplication.data.local.entity.SearchHistoryEntity
import com.example.myapplication.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchHistoryRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override fun getSearchHistory(limit: Int): Flow<List<String>> {
        return searchHistoryDao.getSearchHistory(limit)
            .map { entities -> entities.map { it.query } }
    }

    override suspend fun addSearchHistory(query: String) {
        searchHistoryDao.insertSearchHistory(
            SearchHistoryEntity(query = query)
        )
    }

    override suspend fun deleteSearchHistory(id: Long) {
        searchHistoryDao.deleteSearchHistory(id)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearSearchHistory()
    }

    override fun searchHistory(query: String, limit: Int): Flow<List<String>> {
        return searchHistoryDao.searchHistory(query, limit)
            .map { entities -> entities.map { it.query } }
    }
} 