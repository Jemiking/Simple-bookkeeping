package com.example.myapplication.domain.usecase.settings

import com.example.myapplication.domain.model.Settings
import com.example.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> {
        return settingsRepository.getSettings()
    }
} 