package com.example.myapplication.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.exception.AppException
import com.example.myapplication.core.exception.ErrorHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ErrorState {
    val error: AppException?
}

fun <T : ErrorState> ViewModel.handleError(
    errorHandler: ErrorHandler,
    state: MutableStateFlow<T>,
    throwable: Throwable,
    tag: String? = null
) {
    val appException = AppException.from(throwable)
    errorHandler.handleError(throwable, tag)
    state.update { currentState ->
        @Suppress("UNCHECKED_CAST")
        (currentState as ErrorState)
            .copy(error = appException) as T
    }
}

fun <T : ErrorState> ViewModel.clearError(
    errorHandler: ErrorHandler,
    state: MutableStateFlow<T>
) {
    errorHandler.clearError()
    state.update { currentState ->
        @Suppress("UNCHECKED_CAST")
        (currentState as ErrorState)
            .copy(error = null) as T
    }
}

fun ViewModel.launchWithErrorHandler(
    errorHandler: ErrorHandler,
    state: MutableStateFlow<out ErrorState>,
    tag: String? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: Exception) {
            handleError(errorHandler, state, e, tag)
        }
    }
}

suspend fun <T> withErrorHandler(
    errorHandler: ErrorHandler,
    state: MutableStateFlow<out ErrorState>,
    tag: String? = null,
    block: suspend () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        errorHandler.handleError(e, tag)
        state.update { currentState ->
            @Suppress("UNCHECKED_CAST")
            (currentState as ErrorState)
                .copy(error = AppException.from(e)) as ErrorState
        }
        null
    }
}

fun <T> StateFlow<T>.collectWithErrorHandler(
    viewModel: ViewModel,
    errorHandler: ErrorHandler,
    state: MutableStateFlow<out ErrorState>,
    tag: String? = null,
    block: suspend (T) -> Unit
) {
    viewModel.viewModelScope.launch {
        try {
            collect { value ->
                block(value)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e, tag)
            state.update { currentState ->
                @Suppress("UNCHECKED_CAST")
                (currentState as ErrorState)
                    .copy(error = AppException.from(e)) as ErrorState
            }
        }
    }
} 