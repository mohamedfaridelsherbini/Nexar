package com.mohamedfaridelsherbini.nexar.platform

import platform.Foundation.NSUserDefaults

private const val KEY_ONBOARDING = "onboarding_complete"
private const val KEY_THEME = "app_theme"
private const val KEY_EXPORT_REMINDERS = "export_reminders_enabled"
private const val KEY_DUPLICATE_ALERTS = "duplicate_alerts_enabled"

actual object NexarPrefs {
    actual var isOnboardingComplete: Boolean
        get() = NSUserDefaults.standardUserDefaults.boolForKey(KEY_ONBOARDING)
        set(value) = NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_ONBOARDING)

    actual var appTheme: String
        get() = NSUserDefaults.standardUserDefaults.stringForKey(KEY_THEME) ?: "System"
        set(value) = NSUserDefaults.standardUserDefaults.setObject(value, forKey = KEY_THEME)

    actual var exportRemindersEnabled: Boolean
        get() =
            if (NSUserDefaults.standardUserDefaults.objectForKey(KEY_EXPORT_REMINDERS) == null) {
                true
            } else {
                NSUserDefaults.standardUserDefaults.boolForKey(KEY_EXPORT_REMINDERS)
            }
        set(value) = NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_EXPORT_REMINDERS)

    actual var duplicateAlertsEnabled: Boolean
        get() =
            if (NSUserDefaults.standardUserDefaults.objectForKey(KEY_DUPLICATE_ALERTS) == null) {
                true
            } else {
                NSUserDefaults.standardUserDefaults.boolForKey(KEY_DUPLICATE_ALERTS)
            }
        set(value) = NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_DUPLICATE_ALERTS)
}
