package com.mohamedfaridelsherbini.nexar.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedfaridelsherbini.nexar.domain.usecase.AppTheme
import com.mohamedfaridelsherbini.nexar.domain.usecase.GetSettingsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.platform.PermissionAction
import com.mohamedfaridelsherbini.nexar.platform.PermissionArea
import com.mohamedfaridelsherbini.nexar.platform.PermissionHealth
import com.mohamedfaridelsherbini.nexar.platform.PermissionHealthProvider
import com.mohamedfaridelsherbini.nexar.platform.appVersionName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: AppTheme = AppTheme.System,
    val exportRemindersEnabled: Boolean = true,
    val duplicateAlertsEnabled: Boolean = true,
    val storageLocation: String? = null,
    val permissionHealth: List<PermissionHealth> = emptyList(),
    val version: String = appVersionName(),
)

class SettingsViewModel(
    private val getSettings: GetSettingsUseCase,
    private val observeStorageLocation: ObserveStorageLocationUseCase,
    private val permissionHealthProvider: PermissionHealthProvider,
    private val versionProvider: () -> String = ::appVersionName,
) : ViewModel() {
    private val permissionHealth = MutableStateFlow<List<PermissionHealth>>(emptyList())

    val uiState: StateFlow<SettingsUiState> =
        combine(
            getSettings.observe(),
            observeStorageLocation(),
            permissionHealth,
        ) { settings, location, permissions ->
            SettingsUiState(
                theme = settings.theme,
                exportRemindersEnabled = settings.exportRemindersEnabled,
                duplicateAlertsEnabled = settings.duplicateAlertsEnabled,
                storageLocation = location,
                permissionHealth = permissions,
                version = versionProvider(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(version = versionProvider())
        )

    init {
        refreshPermissionHealth()
    }

    fun onThemeChanged(theme: AppTheme) {
        getSettings.updateTheme(theme)
    }

    fun onExportRemindersToggled(enabled: Boolean) {
        getSettings.updateExportReminders(enabled)
    }

    fun onDuplicateAlertsToggled(enabled: Boolean) {
        getSettings.updateDuplicateAlerts(enabled)
    }

    fun refreshPermissionHealth() {
        viewModelScope.launch {
            permissionHealth.update { permissionHealthProvider.getPermissionHealth() }
        }
    }

    fun onPermissionActionClicked(area: PermissionArea) {
        val action = permissionHealth.value.firstOrNull { it.area == area }?.action ?: PermissionAction.None
        if (action == PermissionAction.OpenSettings) {
            permissionHealthProvider.openAppSettings()
        }
    }
}
