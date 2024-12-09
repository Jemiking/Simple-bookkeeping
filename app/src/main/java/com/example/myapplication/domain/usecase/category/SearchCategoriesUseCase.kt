package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(query: String): Flow<List<Category>> {
        return repository.searchCategories(query)
    }
} 