package com.mohamedfaridelsherbini.nexar.presentation.settings

import com.mohamedfaridelsherbini.nexar.domain.usecase.AppTheme
import com.mohamedfaridelsherbini.nexar.domain.usecase.GetSettingsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SettingsPreferences
import com.mohamedfaridelsherbini.nexar.fakes.FakeStorageRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `uiState combines settings location and version`() = runTest(dispatcher) {
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
            versionProvider = { "9.9.9" },
        )

        val job = activate(viewModel)
        advanceUntilIdle()

        assertEquals(AppTheme.Light, viewModel.uiState.value.theme)
        assertFalse(viewModel.uiState.value.exportRemindersEnabled)
        assertEquals("content://exports", viewModel.uiState.value.storageLocation)
        assertEquals("9.9.9", viewModel.uiState.value.version)
        job.cancel()
    }

    @Test
    fun `actions update downstream state`() = runTest(dispatcher) {
        val prefs = FakeSettingsPreferences()
        val viewModel = SettingsViewModel(
            getSettings = GetSettingsUseCase(prefs),
            observeStorageLocation = ObserveStorageLocationUseCase(FakeStorageRepository()),
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

    private fun kotlinx.coroutines.CoroutineScope.activate(viewModel: SettingsViewModel): Job =
        launch { viewModel.uiState.collect { } }

    private class FakeSettingsPreferences(
        override var appTheme: String = "System",
        override var exportRemindersEnabled: Boolean = true,
        override var duplicateAlertsEnabled: Boolean = true,
    ) : SettingsPreferences
}
