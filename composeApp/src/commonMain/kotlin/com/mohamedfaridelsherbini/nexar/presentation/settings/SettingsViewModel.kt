package com.mohamedfaridelsherbini.nexar.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedfaridelsherbini.nexar.domain.usecase.AppTheme
import com.mohamedfaridelsherbini.nexar.domain.usecase.GetSettingsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.platform.appVersionName
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val theme: AppTheme = AppTheme.System,
    val exportRemindersEnabled: Boolean = true,
    val duplicateAlertsEnabled: Boolean = true,
    val storageLocation: String? = null,
    val version: String = appVersionName(),
)

class SettingsViewModel(
    private val getSettings: GetSettingsUseCase,
    private val observeStorageLocation: ObserveStorageLocationUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(
            getSettings.observe(),
            observeStorageLocation()
        ) { settings, location ->
            SettingsUiState(
                theme = settings.theme,
                exportRemindersEnabled = settings.exportRemindersEnabled,
                duplicateAlertsEnabled = settings.duplicateAlertsEnabled,
                storageLocation = location,
                version = appVersionName(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun onThemeChanged(theme: AppTheme) {
        getSettings.updateTheme(theme)
    }

    fun onExportRemindersToggled(enabled: Boolean) {
        getSettings.updateExportReminders(enabled)
    }

    fun onDuplicateAlertsToggled(enabled: Boolean) {
        getSettings.updateDuplicateAlerts(enabled)
    }
}
