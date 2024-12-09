package com.example.myapplication.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.CategoryType
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.CategoryTrend
import com.example.myapplication.domain.usecase.category.GetCategoriesUseCase
import com.example.myapplication.domain.usecase.statistics.GetMultiCategoryTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CategoryTrendViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMultiCategoryTrendUseCase: GetMultiCategoryTrendUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryTrendState())
    val state: StateFlow<CategoryTrendState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CategoryTrendEffect>()
    val effect: SharedFlow<CategoryTrendEffect> = _effect.asSharedFlow()

    init {
        loadCategories()
    }

    fun onEvent(event: CategoryTrendEvent) {
        when (event) {
            is CategoryTrendEvent.SelectDateRange -> {
                _state.update {
                    it.copy(
                        startMonth = event.startMonth,
                        endMonth = event.endMonth
                    )
                }
                loadTrends()
            }
            is CategoryTrendEvent.ToggleCategory -> {
                _state.update { state ->
                    val selectedIds = state.selectedCategoryIds.toMutableSet()
                    if (selectedIds.contains(event.categoryId)) {
                        selectedIds.remove(event.categoryId)
                    } else {
                        selectedIds.add(event.categoryId)
                    }
                    state.copy(selectedCategoryIds = selectedIds)
                }
                loadTrends()
            }
            CategoryTrendEvent.Refresh -> {
                loadCategories()
                loadTrends()
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                getCategoriesUseCase(CategoryType.EXPENSE).collect { categories ->
                    _state.update { it.copy(categories = categories) }
                }
            } catch (e: Exception) {
                _effect.emit(CategoryTrendEffect.ShowError(e.message ?: "加载分类失败"))
            }
        }
    }

    private fun loadTrends() {
        viewModelScope.launch {
            if (state.value.selectedCategoryIds.isEmpty()) {
                _state.update { it.copy(trends = emptyList()) }
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            try {
                getMultiCategoryTrendUseCase(
                    categoryIds = state.value.selectedCategoryIds.toList(),
                    startYearMonth = state.value.startMonth,
                    endYearMonth = state.value.endMonth
                ).collect { trends ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            trends = trends
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(CategoryTrendEffect.ShowError(e.message ?: "加载趋势数据失败"))
            }
        }
    }
}

data class CategoryTrendState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val startMonth: YearMonth = YearMonth.now().minusMonths(5),
    val endMonth: YearMonth = YearMonth.now(),
    val trends: List<CategoryTrend> = emptyList()
)

sealed class CategoryTrendEffect {
    data class ShowError(val message: String) : CategoryTrendEffect()
}

sealed class CategoryTrendEvent {
    data class SelectDateRange(
        val startMonth: YearMonth,
        val endMonth: YearMonth
    ) : CategoryTrendEvent()
    data class ToggleCategory(val categoryId: Long) : CategoryTrendEvent()
    object Refresh : CategoryTrendEvent()
} 