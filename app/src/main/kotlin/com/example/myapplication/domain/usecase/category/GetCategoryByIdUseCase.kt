package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryByIdUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(id: Long): Result<Category?> {
        return try {
            val category = repository.getCategoryById(id)
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 