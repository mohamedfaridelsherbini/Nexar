package com.mohamedfaridelsherbini.nexar

import android.app.Application
import com.mohamedfaridelsherbini.nexar.di.androidModule
import com.mohamedfaridelsherbini.nexar.di.initKoin
import com.mohamedfaridelsherbini.nexar.platform.NexarNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class NexarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@NexarApplication)
            modules(androidModule)
        }
        NexarNotifier.scheduleExportReminderWorker()
    }
}
