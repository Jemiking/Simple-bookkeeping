package com.example.myapplication.domain.usecase.category

import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(
        type: CategoryType? = null,
        parentId: Long? = null
    ): Flow<List<Category>> {
        return when {
            parentId != null -> {
                repository.getSubCategories(parentId)
            }
            type != null -> {
                repository.getCategoriesByType(type)
            }
            else -> {
                repository.getRootCategories()
            }
        }
    }
} 