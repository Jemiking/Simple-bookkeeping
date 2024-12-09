package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Long, deleteSubCategories: Boolean = true): Result<Unit> {
        return try {
            if (deleteSubCategories) {
                repository.deleteCategoryWithSubCategories(id)
            } else {
                repository.deleteCategory(id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 