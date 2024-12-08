package com.example.myapplication.domain.model

import java.util.UUID
import com.example.myapplication.data.local.entity.TransactionType

data class Category(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val icon: String,
    val color: Int,
    val type: TransactionType,
    val parentId: UUID? = null,
    val isDefault: Boolean = false
) 