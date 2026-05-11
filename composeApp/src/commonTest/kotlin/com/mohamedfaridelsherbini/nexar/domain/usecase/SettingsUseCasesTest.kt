package com.mohamedfaridelsherbini.nexar.domain.usecase

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest

class SettingsUseCasesTest {
    @Test
    fun `invalid stored theme falls back to system`() = runTest {
        val useCase = GetSettingsUseCase(
            FakeSettingsPreferences(appTheme = "Unexpected")
        )

        useCase.observe().test {
            assertEquals(AppTheme.System, awaitItem().theme)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updates persist and emit new settings`() = runTest {
        val prefs = FakeSettingsPreferences()
        val useCase = GetSettingsUseCase(prefs)

        useCase.observe().test {
            assertEquals(SettingsData(AppTheme.System, true, true), awaitItem())

            useCase.updateTheme(AppTheme.Dark)
            assertEquals(AppTheme.Dark, awaitItem().theme)
            assertEquals("Dark", prefs.appTheme)

            useCase.updateExportReminders(false)
            assertFalse(awaitItem().exportRemindersEnabled)
            assertFalse(prefs.exportRemindersEnabled)

            useCase.updateDuplicateAlerts(false)
            assertFalse(awaitItem().duplicateAlertsEnabled)
            assertFalse(prefs.duplicateAlertsEnabled)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeSettingsPreferences(
        override var appTheme: String = "System",
        override var exportRemindersEnabled: Boolean = true,
        override var duplicateAlertsEnabled: Boolean = true,
    ) : SettingsPreferences
}
