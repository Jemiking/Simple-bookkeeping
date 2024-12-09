package com.example.myapplication.domain.usecase.category

import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class CategoryStats(
    val totalCount: Int,
    val countByType: Map<CategoryType, Int>
)

class GetCategoryStatsUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<CategoryStats> {
        return combine(
            repository.getCategoryCount(),
            combine(
                repository.getCategoryCountByType(CategoryType.EXPENSE),
                repository.getCategoryCountByType(CategoryType.INCOME)
            ) { expenseCount, incomeCount ->
                mapOf(
                    CategoryType.EXPENSE to expenseCount,
                    CategoryType.INCOME to incomeCount
                )
            }
        ) { total, countByType ->
            CategoryStats(
                totalCount = total,
                countByType = countByType
            )
        }
    }
} 