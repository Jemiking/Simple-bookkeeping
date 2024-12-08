package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.util.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Unit> {
        return try {
            // 检查是否是默认分类
            if (category.isDefault) {
                return Result.error(IllegalStateException("默认分类不能删除"))
            }

            // 检查是否有关联的交易记录
            val transactionCount = categoryRepository.getTransactionCountForCategory(category.id)
            if (transactionCount > 0) {
                return Result.error(IllegalStateException("该分类下有${transactionCount}条交易记录，不能删除"))
            }

            // 检查是否有子分类
            val subCategories = categoryRepository.getSubCategories(category.id).first()
            if (subCategories.isNotEmpty()) {
                return Result.error(IllegalStateException("请先删除该分类下的所有子分类"))
            }

            // 删除分类
            categoryRepository.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 