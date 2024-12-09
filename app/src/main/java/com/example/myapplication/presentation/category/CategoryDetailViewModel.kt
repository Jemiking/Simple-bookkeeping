package com.example.myapplication.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.usecase.category.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val getCategoryUseCase: GetCategoryUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryDetailState())
    val state: StateFlow<CategoryDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CategoryDetailEffect>()
    val effect: SharedFlow<CategoryDetailEffect> = _effect.asSharedFlow()

    fun loadCategory(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getCategoryUseCase(id).collect { category ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            category = category,
                            name = category.name,
                            note = category.note,
                            type = category.type,
                            color = category.color,
                            icon = category.icon,
                            budgetAmount = category.budgetAmount
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(CategoryDetailEffect.ShowError(e.message ?: "加载分类失败"))
            }
        }
    }

    fun onEvent(event: CategoryDetailEvent) {
        when (event) {
            is CategoryDetailEvent.UpdateName -> {
                _state.update { it.copy(name = event.name) }
            }
            is CategoryDetailEvent.UpdateNote -> {
                _state.update { it.copy(note = event.note) }
            }
            is CategoryDetailEvent.UpdateType -> {
                _state.update { it.copy(type = event.type) }
            }
            is CategoryDetailEvent.UpdateColor -> {
                _state.update { it.copy(color = event.color) }
            }
            is CategoryDetailEvent.UpdateIcon -> {
                _state.update { it.copy(icon = event.icon) }
            }
            is CategoryDetailEvent.UpdateBudget -> {
                _state.update { it.copy(budgetAmount = event.amount) }
            }
            CategoryDetailEvent.SaveCategory -> saveCategory()
            CategoryDetailEvent.DeleteCategory -> deleteCategory()
        }
    }

    private fun saveCategory() {
        viewModelScope.launch {
            try {
                val category = Category(
                    id = state.value.category?.id ?: 0,
                    name = state.value.name,
                    note = state.value.note,
                    type = state.value.type,
                    color = state.value.color,
                    icon = state.value.icon,
                    budgetAmount = if (state.value.type == CategoryType.EXPENSE) state.value.budgetAmount else null,
                    subCategories = state.value.category?.subCategories ?: emptyList()
                )

                if (category.id == 0L) {
                    addCategoryUseCase(category)
                } else {
                    updateCategoryUseCase(category)
                }
                _effect.emit(CategoryDetailEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(CategoryDetailEffect.ShowError(e.message ?: "保存失败"))
            }
        }
    }

    private fun deleteCategory() {
        viewModelScope.launch {
            try {
                state.value.category?.id?.let { id ->
                    deleteCategoryUseCase(id)
                    _effect.emit(CategoryDetailEffect.NavigateBack)
                }
            } catch (e: Exception) {
                _effect.emit(CategoryDetailEffect.ShowError(e.message ?: "删除失败"))
            }
        }
    }
}

data class CategoryDetailState(
    val isLoading: Boolean = false,
    val category: Category? = null,
    val name: String = "",
    val note: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val color: String = "#000000",
    val icon: String = "category",
    val budgetAmount: Double? = null
)

sealed class CategoryDetailEffect {
    object NavigateBack : CategoryDetailEffect()
    data class ShowError(val message: String) : CategoryDetailEffect()
}

sealed class CategoryDetailEvent {
    data class UpdateName(val name: String) : CategoryDetailEvent()
    data class UpdateNote(val note: String) : CategoryDetailEvent()
    data class UpdateType(val type: CategoryType) : CategoryDetailEvent()
    data class UpdateColor(val color: String) : CategoryDetailEvent()
    data class UpdateIcon(val icon: String) : CategoryDetailEvent()
    data class UpdateBudget(val amount: Double) : CategoryDetailEvent()
    object SaveCategory : CategoryDetailEvent()
    object DeleteCategory : CategoryDetailEvent()
} 