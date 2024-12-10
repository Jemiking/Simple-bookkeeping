package com.example.myapplication.domain.usecase.data

import android.net.Uri
import com.example.myapplication.domain.model.ImportResult
import com.example.myapplication.domain.repository.DataRepository
import javax.inject.Inject

class ImportFromExcelUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(uri: Uri): Result<ImportResult> {
        return repository.importFromExcel(uri)
    }
} 