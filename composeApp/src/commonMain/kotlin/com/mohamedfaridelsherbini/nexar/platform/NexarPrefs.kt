package com.mohamedfaridelsherbini.nexar.platform

/**
 * Platform-agnostic lightweight key-value store for app preferences.
 * Mirrors the role of `UserDefaults` (iOS) and `SharedPreferences` (Android).
 */
expect object NexarPrefs {
    /** True once the user has completed the first-launch onboarding flow. */
    var isOnboardingComplete: Boolean
}
