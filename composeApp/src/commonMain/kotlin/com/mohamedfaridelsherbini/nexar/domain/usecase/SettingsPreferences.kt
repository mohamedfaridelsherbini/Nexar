package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.platform.NexarPrefs

interface SettingsPreferences {
    var appTheme: String
    var exportRemindersEnabled: Boolean
    var duplicateAlertsEnabled: Boolean
}

object NexarSettingsPreferences : SettingsPreferences {
    override var appTheme: String
        get() = NexarPrefs.appTheme
        set(value) {
            NexarPrefs.appTheme = value
        }

    override var exportRemindersEnabled: Boolean
        get() = NexarPrefs.exportRemindersEnabled
        set(value) {
            NexarPrefs.exportRemindersEnabled = value
        }

    override var duplicateAlertsEnabled: Boolean
        get() = NexarPrefs.duplicateAlertsEnabled
        set(value) {
            NexarPrefs.duplicateAlertsEnabled = value
        }
}
