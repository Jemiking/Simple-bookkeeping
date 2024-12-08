package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.TransactionType
import java.util.UUID

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val color: Int,
    val type: TransactionType,
    val parentId: String?,
    val isDefault: Boolean
) {
    fun toDomainModel(): Category = Category(
        id = UUID.fromString(id),
        name = name,
        icon = icon,
        color = color,
        type = type,
        parentId = parentId?.let { UUID.fromString(it) },
        isDefault = isDefault
    )

    companion object {
        fun fromDomainModel(category: Category): CategoryEntity = CategoryEntity(
            id = category.id.toString(),
            name = category.name,
            icon = category.icon,
            color = category.color,
            type = category.type,
            parentId = category.parentId?.toString(),
            isDefault = category.isDefault
        )
    }
} 