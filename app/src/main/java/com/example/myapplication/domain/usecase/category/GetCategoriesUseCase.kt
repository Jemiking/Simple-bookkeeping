package com.example.myapplication.domain.usecase.category

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(
        type: TransactionType? = null,
        parentCategoryId: Long? = null
    ): Flow<List<Category>> {
        return when {
            // 获取子分类
            parentCategoryId != null -> {
                categoryRepository.getSubCategories(parentCategoryId)
            }
            // 按类型获取分类
            type != null -> {
                categoryRepository.getCategoriesByType(type)
            }
            // 获取主分类
            else -> {
                categoryRepository.getMainCategories()
            }
        }.catch { e ->
            e.printStackTrace()
            emit(emptyList())
        }.map { categories ->
            // 按排序索引排序
            categories.sortedBy { it.orderIndex }
        }
    }
} 