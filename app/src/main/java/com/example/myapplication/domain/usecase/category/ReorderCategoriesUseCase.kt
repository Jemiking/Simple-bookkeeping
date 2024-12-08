package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.util.Result
import javax.inject.Inject

class ReorderCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categories: List<Category>): Result<Unit> {
        return try {
            // 验证列表不为空
            if (categories.isEmpty()) {
                return Result.error(IllegalArgumentException("分类列表不能为空"))
            }

            // 验证所有分类类型一致
            val firstType = categories.first().type
            if (categories.any { it.type != firstType }) {
                return Result.error(IllegalArgumentException("所有分类必须是同一类型"))
            }

            // 验证所有分类的父分类ID一致
            val firstParentId = categories.first().parentCategoryId
            if (categories.any { it.parentCategoryId != firstParentId }) {
                return Result.error(IllegalArgumentException("所有分类必须属于同一父分类"))
            }

            // 更新顺序
            categoryRepository.reorderCategories(categories.map { it.id })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}