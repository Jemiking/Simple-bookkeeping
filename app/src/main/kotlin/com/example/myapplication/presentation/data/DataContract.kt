package com.example.myapplication.presentation.data

import android.net.Uri
import com.example.myapplication.domain.model.ImportResult

data class DataState(
    val exportProgress: Int = 0,
    val importProgress: Int = 0,
    val importResult: ImportResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class DataEvent {
    data class ExportToCSV(val uri: Uri) : DataEvent()
    data class ImportFromCSV(val uri: Uri) : DataEvent()
    data class ExportToExcel(val uri: Uri) : DataEvent()
    data class ImportFromExcel(val uri: Uri) : DataEvent()
}

sealed class DataEffect {
    object ExportCompleted : DataEffect()
    data class ImportCompleted(val result: ImportResult) : DataEffect()
    data class Error(val message: String) : DataEffect()
} 