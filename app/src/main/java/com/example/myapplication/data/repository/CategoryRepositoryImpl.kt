package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getRootCategories(): Flow<List<Category>> {
        return categoryDao.getRootCategories()
            .combine(
                categoryDao.getRootCategories().flatMapLatest { rootCategories ->
                    combine(
                        rootCategories.map { root ->
                            getSubCategories(root.id)
                        }
                    ) { subCategoriesLists ->
                        subCategoriesLists.toList()
                    }
                }
            ) { rootEntities, subCategories ->
                rootEntities.mapIndexed { index, entity ->
                    Category.fromEntity(entity, subCategories[index])
                }
            }
    }

    override fun getSubCategories(parentId: Long): Flow<List<Category>> {
        return categoryDao.getSubCategories(parentId)
            .map { entities -> entities.map { Category.fromEntity(it) } }
    }

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type)
            .combine(
                categoryDao.getCategoriesByType(type).flatMapLatest { rootCategories ->
                    combine(
                        rootCategories.map { root ->
                            getSubCategories(root.id)
                        }
                    ) { subCategoriesLists ->
                        subCategoriesLists.toList()
                    }
                }
            ) { rootEntities, subCategories ->
                rootEntities.mapIndexed { index, entity ->
                    Category.fromEntity(entity, subCategories[index])
                }
            }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        val category = categoryDao.getCategoryById(id) ?: return null
        val subCategories = getSubCategories(id)
            .first()
            .map { it }
        return Category.fromEntity(category, subCategories)
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertAll(categories.map { it.toEntity() })
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun updateOrderIndex(id: Long, orderIndex: Int) {
        categoryDao.updateOrderIndex(id, orderIndex)
    }

    override suspend fun updateBudgetAmount(id: Long, amount: Double?) {
        categoryDao.updateBudgetAmount(id, amount)
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.softDelete(id)
    }

    override suspend fun deleteCategoryWithSubCategories(id: Long) {
        categoryDao.softDeleteWithSubCategories(id)
    }

    override suspend fun deleteCategories(ids: List<Long>) {
        categoryDao.softDeleteAll(ids)
    }

    override fun searchCategories(query: String): Flow<List<Category>> {
        return categoryDao.searchCategories(query)
            .map { entities -> entities.map { Category.fromEntity(it) } }
    }

    override fun getCategoryCount(): Flow<Int> {
        return categoryDao.getCategoryCount()
    }

    override fun getCategoryCountByType(type: CategoryType): Flow<Int> {
        return categoryDao.getCategoryCountByType(type)
    }
} 