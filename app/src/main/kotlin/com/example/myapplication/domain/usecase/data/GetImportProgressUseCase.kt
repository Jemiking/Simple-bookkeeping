package com.example.myapplication.domain.usecase.data

import com.example.myapplication.domain.repository.DataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImportProgressUseCase @Inject constructor(
    private val repository: DataRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getImportProgress()
    }
} 