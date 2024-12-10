package com.example.myapplication.presentation.category

import com.example.myapplication.data.local.entity.TransactionType
import com.example.myapplication.domain.model.Category

sealed class CategoryEvent {
    // 类型切换
    data class TypeChanged(val type: TransactionType) : CategoryEvent()
    
    // 分类选择
    data class CategorySelected(val category: Category) : CategoryEvent()
    
    // 分类编辑
    data class NameChanged(val name: String) : CategoryEvent()
    data class IconChanged(val icon: String) : CategoryEvent()
    data class ColorChanged(val color: Int) : CategoryEvent()
    data class ParentCategoryChanged(val parentId: Long?) : CategoryEvent()
    
    // 分类操作
    data object ShowAddDialog : CategoryEvent()
    data object HideAddDialog : CategoryEvent()
    data object ShowDeleteDialog : CategoryEvent()
    data object HideDeleteDialog : CategoryEvent()
    data object SaveCategory : CategoryEvent()
    data object DeleteCategory : CategoryEvent()
    
    // 排序相关
    data class StartDragging(val category: Category) : CategoryEvent()
    data class StopDragging : CategoryEvent()
    data class MovedCategory(
        val fromIndex: Int,
        val toIndex: Int
    ) : CategoryEvent()
    
    // 错误处理
    data object DismissError : CategoryEvent()
} 