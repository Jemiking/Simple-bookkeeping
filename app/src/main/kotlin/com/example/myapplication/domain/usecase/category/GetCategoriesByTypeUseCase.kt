package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.CategoryType
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoriesByTypeUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(type: CategoryType): Flow<Result<List<Category>>> {
        return repository.getCategoriesByType(type)
            .map { Result.success(it) }
            .catch { e -> emit(Result.failure(e)) }
    }
} 