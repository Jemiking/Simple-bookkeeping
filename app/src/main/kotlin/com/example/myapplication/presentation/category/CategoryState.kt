package com.example.myapplication.presentation.category

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Category

data class CategoryState(
    // 分类列表
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    
    // 当前选择
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    
    // 编辑状态
    val isEditing: Boolean = false,
    val editingCategory: EditingCategory = EditingCategory(),
    
    // UI状态
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDragging: Boolean = false
)

data class EditingCategory(
    val id: Long = 0,
    val name: String = "",
    val icon: String = "",
    val color: Int = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val orderIndex: Int = 0,
    val isDefault: Boolean = false,
    val parentCategoryId: Long? = null,
    
    // 验证错误
    val nameError: String? = null,
    val iconError: String? = null
) 