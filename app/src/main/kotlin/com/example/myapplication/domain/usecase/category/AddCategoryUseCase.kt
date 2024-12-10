package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Long> {
        return try {
            val id = repository.insertCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 