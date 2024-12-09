package com.example.myapplication.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.repository.CategoryRepository
import com.example.myapplication.domain.usecase.category.DeleteCategoryUseCase
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.category.GetCategoryStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCategoryStatsUseCase: GetCategoryStatsUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryState())
    val state: StateFlow<CategoryState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CategoryEffect>()
    val effect: SharedFlow<CategoryEffect> = _effect.asSharedFlow()

    init {
        loadCategories()
        loadStats()
    }

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.LoadCategories -> loadCategories(event.type)
            is CategoryEvent.DeleteCategory -> deleteCategory(event.categoryId)
            is CategoryEvent.RefreshStats -> loadStats()
        }
    }

    private fun loadCategories(type: CategoryType? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getCategoriesUseCase(type).collect { categories ->
                    _state.update { it.copy(
                        categories = categories,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(CategoryEffect.ShowError(e.message ?: "加载分类失败"))
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                getCategoryStatsUseCase().collect { stats ->
                    _state.update { it.copy(stats = stats) }
                }
            } catch (e: Exception) {
                _effect.emit(CategoryEffect.ShowError(e.message ?: "加载统计信息失败"))
            }
        }
    }

    private fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId)
                _effect.emit(CategoryEffect.ShowMessage("分类删除成功"))
                loadCategories()
                loadStats()
            } catch (e: Exception) {
                _effect.emit(CategoryEffect.ShowError(e.message ?: "删除分类失败"))
            }
        }
    }
}

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val stats: CategoryStats? = null,
    val isLoading: Boolean = false
)

sealed class CategoryEffect {
    data class ShowMessage(val message: String) : CategoryEffect()
    data class ShowError(val message: String) : CategoryEffect()
}

sealed class CategoryEvent {
    data class LoadCategories(val type: CategoryType? = null) : CategoryEvent()
    data class DeleteCategory(val categoryId: Long) : CategoryEvent()
    object RefreshStats : CategoryEvent()
}

data class CategoryStats(
    val totalCount: Int,
    val countByType: Map<CategoryType, Int>
) 