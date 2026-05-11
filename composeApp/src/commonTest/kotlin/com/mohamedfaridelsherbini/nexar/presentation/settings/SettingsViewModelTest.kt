package com.mohamedfaridelsherbini.nexar.presentation.settings

import com.mohamedfaridelsherbini.nexar.domain.usecase.AppTheme
import com.mohamedfaridelsherbini.nexar.domain.usecase.GetSettingsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SettingsPreferences
import com.mohamedfaridelsherbini.nexar.fakes.FakeStorageRepository
import com.mohamedfaridelsherbini.nexar.platform.PermissionAction
import com.mohamedfaridelsherbini.nexar.platform.PermissionArea
import com.mohamedfaridelsherbini.nexar.platform.PermissionHealth
import com.mohamedfaridelsherbini.nexar.platform.PermissionHealthProvider
import com.mohamedfaridelsherbini.nexar.platform.PermissionState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN permission health and settings WHEN uiState is collected THEN combined settings state is exposed`() = runTest(dispatcher) {
        val prefs = FakeSettingsPreferences(
            appTheme = "Light",
            exportRemindersEnabled = false,
            duplicateAlertsEnabled = true,
        )
        val storageRepo = FakeStorageRepository().apply {
            currentLocation = "content://exports"
        }
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(prefs),
            observeStorageLocation = ObserveStorageLocationUseCase(storageRepo),
            permissionHealthProvider = FakePermissionHealthProvider(),
            versionProvider = { "9.9.9" },
        )

        val job = activate(viewModel)
        advanceUntilIdle()

        assertEquals(AppTheme.Light, viewModel.uiState.value.theme)
        assertFalse(viewModel.uiState.value.exportRemindersEnabled)
        assertEquals("content://exports", viewModel.uiState.value.storageLocation)
        assertEquals("9.9.9", viewModel.uiState.value.version)
        assertEquals(
            listOf(PermissionArea.Camera, PermissionArea.Files),
            viewModel.uiState.value.permissionHealth.map { it.area }
        )
        job.cancel()
    }

    @Test
    fun `GIVEN settings actions WHEN toggles are changed THEN downstream state is updated`() = runTest(dispatcher) {
        val prefs = FakeSettingsPreferences()
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(prefs),
            observeStorageLocation = ObserveStorageLocationUseCase(FakeStorageRepository()),
            permissionHealthProvider = FakePermissionHealthProvider(),
            versionProvider = { "1.0.0" },
        )

        val job = activate(viewModel)
        advanceUntilIdle()

        viewModel.onThemeChanged(AppTheme.Dark)
        viewModel.onExportRemindersToggled(false)
        viewModel.onDuplicateAlertsToggled(false)
        advanceUntilIdle()

        assertEquals(AppTheme.Dark, viewModel.uiState.value.theme)
        assertFalse(viewModel.uiState.value.exportRemindersEnabled)
        assertFalse(viewModel.uiState.value.duplicateAlertsEnabled)
        assertEquals("Dark", prefs.appTheme)
        job.cancel()
    }

    @Test
    fun `GIVEN denied permission row WHEN open settings action is clicked THEN provider opens app settings`() = runTest(dispatcher) {
        val provider = FakePermissionHealthProvider(
            permissionHealth = listOf(
                PermissionHealth(
                    area = PermissionArea.Notifications,
                    state = PermissionState.Denied,
                    detail = "Blocked",
                    action = PermissionAction.OpenSettings,
                )
            )
        )
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(FakeSettingsPreferences()),
            observeStorageLocation = ObserveStorageLocationUseCase(FakeStorageRepository()),
            permissionHealthProvider = provider,
            versionProvider = { "1.0.0" },
        )

        val job = activate(viewModel)
        advanceUntilIdle()
        viewModel.onPermissionActionClicked(PermissionArea.Notifications)

        assertTrue(provider.openSettingsCalls == 1)
        job.cancel()
    }

    private fun kotlinx.coroutines.CoroutineScope.activate(viewModel: SettingsViewModel): Job =
        launch { viewModel.uiState.collect { } }

    private class FakeSettingsPreferences(
        override var appTheme: String = "System",
        override var exportRemindersEnabled: Boolean = true,
        override var duplicateAlertsEnabled: Boolean = true,
    ) : SettingsPreferences

    private class FakePermissionHealthProvider(
        private val permissionHealth: List<PermissionHealth> = listOf(
            PermissionHealth(
                area = PermissionArea.Camera,
                state = PermissionState.Granted,
                detail = "Ready",
            ),
            PermissionHealth(
                area = PermissionArea.Files,
                state = PermissionState.NotRequired,
                detail = "System picker",
            ),
        )
    ) : PermissionHealthProvider {
        var openSettingsCalls = 0
            private set

        override suspend fun getPermissionHealth(): List<PermissionHealth> = permissionHealth

        override fun openAppSettings() {
            openSettingsCalls++
        }
    }
}
