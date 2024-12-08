package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface CategoryRepository {
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: UUID)
    suspend fun getCategoryById(categoryId: UUID): Category?
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>
    suspend fun getDefaultCategories(): List<Category>
    suspend fun getCategoryCount(): Int
} 