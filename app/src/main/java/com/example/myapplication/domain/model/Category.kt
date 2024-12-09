package com.example.myapplication.domain.model

import com.example.myapplication.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.CategoryType
import java.time.LocalDateTime

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val color: String,
    val parentId: Long? = null,
    val orderIndex: Int = 0,
    val budgetAmount: Double? = null,
    val note: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean = false,
    val subCategories: List<Category> = emptyList()
) {
    companion object {
        fun fromEntity(entity: CategoryEntity, subCategories: List<Category> = emptyList()): Category {
            return Category(
                id = entity.id,
                name = entity.name,
                type = entity.type,
                icon = entity.icon,
                color = entity.color,
                parentId = entity.parentId,
                orderIndex = entity.orderIndex,
                budgetAmount = entity.budgetAmount,
                note = entity.note,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isDeleted = entity.isDeleted,
                subCategories = subCategories
            )
        }
    }

    fun toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            type = type,
            icon = icon,
            color = color,
            parentId = parentId,
            orderIndex = orderIndex,
            budgetAmount = budgetAmount,
            note = note,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }
} 