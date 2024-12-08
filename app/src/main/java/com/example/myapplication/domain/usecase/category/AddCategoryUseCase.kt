package com.example.myapplication.domain.usecase.category

import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.util.Result
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: Category): Result<Long> {
        return try {
            // 验证分类名称
            if (category.name.isBlank()) {
                return Result.error(IllegalArgumentException("分类名称不能为空"))
            }

            // 验证图标
            if (category.icon.isBlank()) {
                return Result.error(IllegalArgumentException("请选择分类图标"))
            }

            // 如果是子分类，验证父分类是否存在
            if (category.parentCategoryId != null) {
                val parentCategory = categoryRepository.getCategoryById(category.parentCategoryId)
                    ?: return Result.error(IllegalArgumentException("父分类不存在"))

                // 验证父子分类类型是否一致
                if (parentCategory.type != category.type) {
                    return Result.error(IllegalArgumentException("子分类类型必须与父分类一致"))
                }
            }

            // 添加分类
            val id = categoryRepository.addCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
} 