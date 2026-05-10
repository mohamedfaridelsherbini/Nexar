package com.mohamedfaridelsherbini.nexar

import android.app.Application
import com.mohamedfaridelsherbini.nexar.data.db.initDatabase
import com.mohamedfaridelsherbini.nexar.di.androidModule
import com.mohamedfaridelsherbini.nexar.di.initKoin
import com.mohamedfaridelsherbini.nexar.domain.usecase.initOcr
import com.mohamedfaridelsherbini.nexar.platform.NexarNotifier
import com.mohamedfaridelsherbini.nexar.platform.initHaptic
import com.mohamedfaridelsherbini.nexar.platform.initNexarNotifier
import com.mohamedfaridelsherbini.nexar.platform.initNexarPrefs
import com.mohamedfaridelsherbini.nexar.platform.initShare
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class NexarApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize platform services immediately with application context
        initDatabase(this)
        initOcr(this)
        initHaptic(this)
        initShare(this)
        initNexarPrefs(this)
        initNexarNotifier(this)

        initKoin {
            androidLogger()
            androidContext(this@NexarApplication)
            modules(androidModule)
        }
        NexarNotifier.scheduleExportReminderWorker()
    }
}
