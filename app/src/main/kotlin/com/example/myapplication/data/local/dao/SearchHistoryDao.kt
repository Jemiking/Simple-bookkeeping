package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getSearchHistory(limit: Int = 10): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(searchHistory: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchHistory(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("""
        SELECT * FROM search_history 
        WHERE query LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun searchHistory(query: String, limit: Int = 5): Flow<List<SearchHistoryEntity>>
} 