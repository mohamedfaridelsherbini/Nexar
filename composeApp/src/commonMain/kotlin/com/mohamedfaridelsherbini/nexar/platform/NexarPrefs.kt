package com.mohamedfaridelsherbini.nexar.platform

/**
 * Platform-agnostic lightweight key-value store for app preferences.
 * Mirrors the role of `UserDefaults` (iOS) and `SharedPreferences` (Android).
 */
expect object NexarPrefs {
    /** True once the user has completed the first-launch onboarding flow. */
    var isOnboardingComplete: Boolean

    /** The preferred UI theme: "System", "Light", or "Dark". Defaults to "System". */
    var appTheme: String

    /** Whether the app should notify the user to export pending documents. Defaults to true. */
    var exportRemindersEnabled: Boolean

    /** Whether the app should alert the user when a possible duplicate is detected. Defaults to true. */
    var duplicateAlertsEnabled: Boolean
}
