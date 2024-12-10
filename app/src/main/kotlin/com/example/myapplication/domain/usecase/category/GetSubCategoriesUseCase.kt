package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSubCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(parentId: Long): Flow<Result<List<Category>>> {
        return repository.getSubCategories(parentId)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 