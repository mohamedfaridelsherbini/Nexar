package com.mohamedfaridelsherbini.nexar.platform

import platform.Foundation.NSUserDefaults

private const val KEY_ONBOARDING = "onboarding_complete"

actual object NexarPrefs {
    actual var isOnboardingComplete: Boolean
        get() = NSUserDefaults.standardUserDefaults.boolForKey(KEY_ONBOARDING)
        set(value) = NSUserDefaults.standardUserDefaults.setBool(value, forKey = KEY_ONBOARDING)
}
