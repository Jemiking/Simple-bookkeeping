package com.example.myapplication.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun getSearchHistory(limit: Int = 10): Flow<List<String>>
    suspend fun addSearchHistory(query: String)
    suspend fun deleteSearchHistory(id: Long)
    suspend fun clearSearchHistory()
    fun searchHistory(query: String, limit: Int = 5): Flow<List<String>>
} 