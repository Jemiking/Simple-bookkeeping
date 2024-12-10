package com.example.myapplication.presentation.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val exportToCSVUseCase: ExportToCSVUseCase,
    private val importFromCSVUseCase: ImportFromCSVUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase,
    private val importFromExcelUseCase: ImportFromExcelUseCase,
    private val getExportProgressUseCase: GetExportProgressUseCase,
    private val getImportProgressUseCase: GetImportProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DataState())
    val state: StateFlow<DataState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DataEffect>()
    val effect: SharedFlow<DataEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            getExportProgressUseCase()
                .collect { progress ->
                    _state.update { it.copy(exportProgress = progress) }
                }
        }

        viewModelScope.launch {
            getImportProgressUseCase()
                .collect { progress ->
                    _state.update { it.copy(importProgress = progress) }
                }
        }
    }

    fun onEvent(event: DataEvent) {
        when (event) {
            is DataEvent.ExportToCSV -> exportToCSV(event.uri)
            is DataEvent.ImportFromCSV -> importFromCSV(event.uri)
            is DataEvent.ExportToExcel -> exportToExcel(event.uri)
            is DataEvent.ImportFromExcel -> importFromExcel(event.uri)
        }
    }

    private fun exportToCSV(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            exportToCSVUseCase(uri)
                .onSuccess {
                    _effect.emit(DataEffect.ExportCompleted)
                }
                .onFailure { error ->
                    _effect.emit(DataEffect.Error(error.message ?: "Unknown error"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun importFromCSV(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            importFromCSVUseCase(uri)
                .onSuccess { result ->
                    _state.update { it.copy(importResult = result) }
                    _effect.emit(DataEffect.ImportCompleted(result))
                }
                .onFailure { error ->
                    _effect.emit(DataEffect.Error(error.message ?: "Unknown error"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun exportToExcel(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            exportToExcelUseCase(uri)
                .onSuccess {
                    _effect.emit(DataEffect.ExportCompleted)
                }
                .onFailure { error ->
                    _effect.emit(DataEffect.Error(error.message ?: "Unknown error"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun importFromExcel(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            importFromExcelUseCase(uri)
                .onSuccess { result ->
                    _state.update { it.copy(importResult = result) }
                    _effect.emit(DataEffect.ImportCompleted(result))
                }
                .onFailure { error ->
                    _effect.emit(DataEffect.Error(error.message ?: "Unknown error"))
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
} 