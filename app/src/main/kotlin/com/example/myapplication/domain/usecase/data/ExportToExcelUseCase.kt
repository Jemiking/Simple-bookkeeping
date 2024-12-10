package com.example.myapplication.domain.usecase.data

import android.net.Uri
import com.example.myapplication.domain.repository.DataRepository
import javax.inject.Inject

class ExportToExcelUseCase @Inject constructor(
    private val repository: DataRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Unit> {
        return repository.exportToExcel(uri)
    }
} 