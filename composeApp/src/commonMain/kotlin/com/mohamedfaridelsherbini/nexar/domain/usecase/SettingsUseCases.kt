package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.platform.NexarPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppTheme(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark"),
}

class GetSettingsUseCase {
    private val _settings = MutableStateFlow(
        SettingsData(
            theme = AppTheme.entries.find { it.label == NexarPrefs.appTheme } ?: AppTheme.System,
            exportRemindersEnabled = NexarPrefs.exportRemindersEnabled,
            duplicateAlertsEnabled = NexarPrefs.duplicateAlertsEnabled
        )
    )

    fun observe(): Flow<SettingsData> = _settings.asStateFlow()

    fun updateTheme(theme: AppTheme) {
        NexarPrefs.appTheme = theme.label
        _settings.update { it.copy(theme = theme) }
    }

    fun updateExportReminders(enabled: Boolean) {
        NexarPrefs.exportRemindersEnabled = enabled
        _settings.update { it.copy(exportRemindersEnabled = enabled) }
    }

    fun updateDuplicateAlerts(enabled: Boolean) {
        NexarPrefs.duplicateAlertsEnabled = enabled
        _settings.update { it.copy(duplicateAlertsEnabled = enabled) }
    }
}

data class SettingsData(
    val theme: AppTheme,
    val exportRemindersEnabled: Boolean,
    val duplicateAlertsEnabled: Boolean,
)
