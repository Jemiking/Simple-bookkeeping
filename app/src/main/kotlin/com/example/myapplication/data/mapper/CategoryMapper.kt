package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.domain.model.Category

fun CategoryEntity.toDomain(
    subCategories: List<Category> = emptyList(),
    transactionCount: Int = 0
): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        orderIndex = orderIndex,
        isDefault = isDefault,
        parentCategoryId = parentCategoryId,
        subCategories = subCategories,
        transactionCount = transactionCount
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        orderIndex = orderIndex,
        isDefault = isDefault,
        parentCategoryId = parentCategoryId
    )
} 