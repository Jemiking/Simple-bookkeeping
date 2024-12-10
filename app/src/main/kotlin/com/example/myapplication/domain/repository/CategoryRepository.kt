package com.example.myapplication.domain.repository

import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getRootCategories(): Flow<List<Category>>
    
    fun getSubCategories(parentId: Long): Flow<List<Category>>
    
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>>
    
    suspend fun getCategoryById(id: Long): Category?
    
    suspend fun insertCategory(category: Category): Long
    
    suspend fun insertCategories(categories: List<Category>)
    
    suspend fun updateCategory(category: Category)
    
    suspend fun updateOrderIndex(id: Long, orderIndex: Int)
    
    suspend fun updateBudgetAmount(id: Long, amount: Double?)
    
    suspend fun deleteCategory(id: Long)
    
    suspend fun deleteCategoryWithSubCategories(id: Long)
    
    suspend fun deleteCategories(ids: List<Long>)
    
    fun searchCategories(query: String): Flow<List<Category>>
    
    fun getCategoryCount(): Flow<Int>
    
    fun getCategoryCountByType(type: CategoryType): Flow<Int>
} 