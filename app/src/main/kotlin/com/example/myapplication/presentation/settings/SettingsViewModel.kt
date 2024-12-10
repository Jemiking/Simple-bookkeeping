package com.example.myapplication.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.usecase.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val dataManagementUseCase: DataManagementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        loadSettings()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateTheme -> updateTheme(event.theme)
            is SettingsEvent.UpdateLanguage -> updateLanguage(event.language)
            is SettingsEvent.UpdateCurrency -> updateCurrency(event.currency)
            is SettingsEvent.UpdateStartDayOfMonth -> updateStartDayOfMonth(event.day)
            is SettingsEvent.UpdateShowDecimal -> updateShowDecimal(event.show)
            is SettingsEvent.UpdateBiometric -> updateBiometric(event.enable)
            is SettingsEvent.UpdateBackup -> updateBackup(event.enable)
            is SettingsEvent.UpdateBackupFrequency -> updateBackupFrequency(event.frequency)
            is SettingsEvent.UpdateBackupPath -> updateBackupPath(event.path)
            is SettingsEvent.UpdateNotification -> updateNotification(event.enable)
            is SettingsEvent.UpdateNotificationTime -> updateNotificationTime(event.time)
            is SettingsEvent.UpdateBudgetAlert -> updateBudgetAlert(event.enable)
            is SettingsEvent.UpdateBudgetAlertThreshold -> updateBudgetAlertThreshold(event.threshold)
            SettingsEvent.ExportData -> exportData()
            SettingsEvent.ImportData -> importData()
            SettingsEvent.ClearData -> clearData()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                _state.update {
                    it.copy(
                        theme = settings.theme,
                        language = settings.language,
                        currency = settings.currency,
                        startDayOfMonth = settings.startDayOfMonth,
                        showDecimal = settings.showDecimal,
                        enableBiometric = settings.enableBiometric,
                        enableBackup = settings.enableBackup,
                        backupFrequency = settings.backupFrequency,
                        backupPath = settings.backupPath,
                        enableNotification = settings.enableNotification,
                        notificationTime = settings.notificationTime,
                        enableBudgetAlert = settings.enableBudgetAlert,
                        budgetAlertThreshold = settings.budgetAlertThreshold
                    )
                }
            }
        }
    }

    private fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateTheme(theme)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新主题失败"))
            }
        }
    }

    private fun updateLanguage(language: Language) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateLanguage(language)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新语言失败"))
            }
        }
    }

    private fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateCurrency(currency)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新货币失败"))
            }
        }
    }

    private fun updateStartDayOfMonth(day: Int) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateStartDayOfMonth(day)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新记账起始日失败"))
            }
        }
    }

    private fun updateShowDecimal(show: Boolean) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateShowDecimal(show)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新小数显示失败"))
            }
        }
    }

    private fun updateBiometric(enable: Boolean) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBiometric(enable)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新生物识别失败"))
            }
        }
    }

    private fun updateBackup(enable: Boolean) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBackup(enable)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新备份设置失败"))
            }
        }
    }

    private fun updateBackupFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBackup(
                    enable = state.value.enableBackup,
                    frequency = frequency
                )
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新备份频率失败"))
            }
        }
    }

    private fun updateBackupPath(path: String) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBackup(
                    enable = state.value.enableBackup,
                    path = path
                )
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新备份路径失败"))
            }
        }
    }

    private fun updateNotification(enable: Boolean) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateNotification(enable)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新通知设置失败"))
            }
        }
    }

    private fun updateNotificationTime(time: String) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateNotification(
                    enable = state.value.enableNotification,
                    time = time
                )
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新通知时间失败"))
            }
        }
    }

    private fun updateBudgetAlert(enable: Boolean) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBudgetAlert(enable)
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新预算提醒失败"))
            }
        }
    }

    private fun updateBudgetAlertThreshold(threshold: Int) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateBudgetAlert(
                    enable = state.value.enableBudgetAlert,
                    threshold = threshold
                )
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "更新预算提醒阈值失败"))
            }
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            try {
                val path = "${System.currentTimeMillis()}.db"
                dataManagementUseCase.exportData(path)
                    .onSuccess {
                        _effect.emit(SettingsEffect.ShowMessage("导出成功"))
                    }
                    .onFailure { e ->
                        _effect.emit(SettingsEffect.ShowError(e.message ?: "导出失败"))
                    }
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "导出失败"))
            }
        }
    }

    private fun importData() {
        viewModelScope.launch {
            try {
                // TODO: 实现文件选择
                val path = ""
                dataManagementUseCase.importData(path)
                    .onSuccess {
                        _effect.emit(SettingsEffect.ShowMessage("导入成功"))
                    }
                    .onFailure { e ->
                        _effect.emit(SettingsEffect.ShowError(e.message ?: "导入失败"))
                    }
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "导入失败"))
            }
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            try {
                dataManagementUseCase.clearData()
                    .onSuccess {
                        _effect.emit(SettingsEffect.ShowMessage("清除成功"))
                    }
                    .onFailure { e ->
                        _effect.emit(SettingsEffect.ShowError(e.message ?: "清除失败"))
                    }
            } catch (e: Exception) {
                _effect.emit(SettingsEffect.ShowError(e.message ?: "清除失败"))
            }
        }
    }
}

data class SettingsState(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.SYSTEM,
    val currency: Currency = Currency.CNY,
    val startDayOfMonth: Int = 1,
    val showDecimal: Boolean = true,
    val enableBiometric: Boolean = false,
    val enableBackup: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.WEEKLY,
    val backupPath: String = "",
    val enableNotification: Boolean = true,
    val notificationTime: String = "20:00",
    val enableBudgetAlert: Boolean = true,
    val budgetAlertThreshold: Int = 80
)

sealed class SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect()
    data class ShowError(val message: String) : SettingsEffect()
}

sealed class SettingsEvent {
    data class UpdateTheme(val theme: Theme) : SettingsEvent()
    data class UpdateLanguage(val language: Language) : SettingsEvent()
    data class UpdateCurrency(val currency: Currency) : SettingsEvent()
    data class UpdateStartDayOfMonth(val day: Int) : SettingsEvent()
    data class UpdateShowDecimal(val show: Boolean) : SettingsEvent()
    data class UpdateBiometric(val enable: Boolean) : SettingsEvent()
    data class UpdateBackup(val enable: Boolean) : SettingsEvent()
    data class UpdateBackupFrequency(val frequency: BackupFrequency) : SettingsEvent()
    data class UpdateBackupPath(val path: String) : SettingsEvent()
    data class UpdateNotification(val enable: Boolean) : SettingsEvent()
    data class UpdateNotificationTime(val time: String) : SettingsEvent()
    data class UpdateBudgetAlert(val enable: Boolean) : SettingsEvent()
    data class UpdateBudgetAlertThreshold(val threshold: Int) : SettingsEvent()
    object ExportData : SettingsEvent()
    object ImportData : SettingsEvent()
    object ClearData : SettingsEvent()
} 