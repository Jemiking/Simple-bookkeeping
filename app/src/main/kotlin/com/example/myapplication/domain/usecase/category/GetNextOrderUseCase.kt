package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.repository.CategoryRepository
import javax.inject.Inject

class GetNextOrderUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend operator fun invoke(parentId: Long?): Result<Int> {
        return try {
            val order = repository.getNextOrder(parentId)
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 