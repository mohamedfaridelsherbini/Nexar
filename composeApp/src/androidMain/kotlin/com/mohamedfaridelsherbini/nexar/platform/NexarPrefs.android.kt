package com.mohamedfaridelsherbini.nexar.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS_NAME = "nexar_prefs"
private const val KEY_ONBOARDING = "onboarding_complete"
private const val KEY_THEME = "app_theme"
private const val KEY_EXPORT_REMINDERS = "export_reminders_enabled"
private const val KEY_DUPLICATE_ALERTS = "duplicate_alerts_enabled"

private lateinit var prefs: SharedPreferences

/** Must be called once in [NexarApplication.onCreate] before any composable runs. */
fun initNexarPrefs(context: Context) {
    prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

actual object NexarPrefs {
    actual var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING, value) }

    actual var appTheme: String
        get() = prefs.getString(KEY_THEME, "System") ?: "System"
        set(value) = prefs.edit { putString(KEY_THEME, value) }

    actual var exportRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_EXPORT_REMINDERS, true)
        set(value) = prefs.edit { putBoolean(KEY_EXPORT_REMINDERS, value) }

    actual var duplicateAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_DUPLICATE_ALERTS, true)
        set(value) = prefs.edit { putBoolean(KEY_DUPLICATE_ALERTS, value) }
}
