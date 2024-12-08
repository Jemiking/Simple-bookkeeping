package com.example.myapplication.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.usecase.category.AddCategoryUseCase
import com.example.myapplication.domain.usecase.category.DeleteCategoryUseCase
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.category.ReorderCategoriesUseCase
import com.example.myapplication.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val addCategory: AddCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val reorderCategories: ReorderCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state = combine(
        _state,
        getCategories()
    ) { state, categories ->
        state.copy(
            expenseCategories = categories.filter { 
                it.type == com.example.myapplication.data.local.entity.TransactionType.EXPENSE 
            }.sortedBy { it.orderIndex },
            incomeCategories = categories.filter { 
                it.type == com.example.myapplication.data.local.entity.TransactionType.INCOME 
            }.sortedBy { it.orderIndex }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryState()
    )

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.TypeChanged -> {
                _state.value = _state.value.copy(
                    selectedType = event.type,
                    selectedCategory = null
                )
            }
            is CategoryEvent.CategorySelected -> {
                _state.value = _state.value.copy(
                    selectedCategory = event.category,
                    isEditing = true,
                    editingCategory = event.category.toEditingCategory()
                )
            }
            is CategoryEvent.NameChanged -> {
                _state.value = _state.value.copy(
                    editingCategory = _state.value.editingCategory.copy(
                        name = event.name,
                        nameError = null
                    )
                )
            }
            is CategoryEvent.IconChanged -> {
                _state.value = _state.value.copy(
                    editingCategory = _state.value.editingCategory.copy(
                        icon = event.icon,
                        iconError = null
                    )
                )
            }
            is CategoryEvent.ColorChanged -> {
                _state.value = _state.value.copy(
                    editingCategory = _state.value.editingCategory.copy(
                        color = event.color
                    )
                )
            }
            is CategoryEvent.ParentCategoryChanged -> {
                _state.value = _state.value.copy(
                    editingCategory = _state.value.editingCategory.copy(
                        parentCategoryId = event.parentId
                    )
                )
            }
            CategoryEvent.ShowAddDialog -> {
                _state.value = _state.value.copy(
                    isAddDialogVisible = true,
                    isEditing = false,
                    editingCategory = EditingCategory(
                        type = _state.value.selectedType
                    )
                )
            }
            CategoryEvent.HideAddDialog -> {
                _state.value = _state.value.copy(
                    isAddDialogVisible = false,
                    isEditing = false,
                    editingCategory = EditingCategory()
                )
            }
            CategoryEvent.ShowDeleteDialog -> {
                _state.value = _state.value.copy(
                    isDeleteDialogVisible = true
                )
            }
            CategoryEvent.HideDeleteDialog -> {
                _state.value = _state.value.copy(
                    isDeleteDialogVisible = false
                )
            }
            CategoryEvent.SaveCategory -> {
                saveCategory()
            }
            CategoryEvent.DeleteCategory -> {
                deleteSelectedCategory()
            }
            is CategoryEvent.StartDragging -> {
                _state.value = _state.value.copy(
                    isDragging = true,
                    selectedCategory = event.category
                )
            }
            CategoryEvent.StopDragging -> {
                _state.value = _state.value.copy(
                    isDragging = false,
                    selectedCategory = null
                )
            }
            is CategoryEvent.MovedCategory -> {
                reorderCategory(event.fromIndex, event.toIndex)
            }
            CategoryEvent.DismissError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun saveCategory() {
        val currentState = _state.value
        val editingCategory = currentState.editingCategory

        // 验证输入
        if (!validateInput()) {
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)

            val category = Category(
                id = if (currentState.isEditing) {
                    currentState.selectedCategory?.id ?: 0
                } else 0,
                name = editingCategory.name,
                icon = editingCategory.icon,
                color = editingCategory.color,
                type = editingCategory.type,
                orderIndex = editingCategory.orderIndex,
                isDefault = editingCategory.isDefault,
                parentCategoryId = editingCategory.parentCategoryId
            )

            when (val result = addCategory(category)) {
                is Result.Success -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        isAddDialogVisible = false,
                        isEditing = false,
                        editingCategory = EditingCategory()
                    )
                }
                is Result.Error -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun deleteSelectedCategory() {
        val currentState = _state.value
        val selectedCategory = currentState.selectedCategory ?: return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)

            when (val result = deleteCategory(selectedCategory)) {
                is Result.Success -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        isDeleteDialogVisible = false,
                        selectedCategory = null
                    )
                }
                is Result.Error -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun reorderCategory(fromIndex: Int, toIndex: Int) {
        val currentState = _state.value
        val categories = if (currentState.selectedType == com.example.myapplication.data.local.entity.TransactionType.EXPENSE) {
            currentState.expenseCategories
        } else {
            currentState.incomeCategories
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)

            when (val result = reorderCategories(categories, fromIndex, toIndex)) {
                is Result.Success -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        isDragging = false
                    )
                }
                is Result.Error -> {
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun validateInput(): Boolean {
        val currentState = _state.value
        val editingCategory = currentState.editingCategory
        var isValid = true

        // 验证名称
        if (editingCategory.name.isBlank()) {
            _state.value = currentState.copy(
                editingCategory = editingCategory.copy(
                    nameError = "请输入分类名称"
                )
            )
            isValid = false
        }

        // 验证图标
        if (editingCategory.icon.isBlank()) {
            _state.value = currentState.copy(
                editingCategory = editingCategory.copy(
                    iconError = "请选择分类图标"
                )
            )
            isValid = false
        }

        return isValid
    }
}

private fun Category.toEditingCategory(): EditingCategory {
    return EditingCategory(
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