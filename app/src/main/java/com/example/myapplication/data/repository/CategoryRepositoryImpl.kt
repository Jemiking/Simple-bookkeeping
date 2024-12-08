package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(CategoryEntity.fromDomainModel(category))
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(CategoryEntity.fromDomainModel(category))
    }

    override suspend fun deleteCategory(categoryId: UUID) {
        categoryDao.deleteCategory(categoryId.toString())
    }

    override suspend fun getCategoryById(categoryId: UUID): Category? {
        return categoryDao.getCategoryById(categoryId.toString())?.toDomainModel()
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getDefaultCategories(): List<Category> {
        return categoryDao.getDefaultCategories().map { it.toDomainModel() }
    }

    override suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }
} 