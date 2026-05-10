package com.mohamedfaridelsherbini.nexar.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS_NAME = "nexar_prefs"
private const val KEY_ONBOARDING = "onboarding_complete"

private lateinit var prefs: SharedPreferences

/** Must be called once in [NexarApplication.onCreate] before any composable runs. */
fun initNexarPrefs(context: Context) {
    prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

actual object NexarPrefs {
    actual var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING, value) }
}
