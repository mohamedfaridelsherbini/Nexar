package com.mohamedfaridelsherbini.nexar

import android.content.Context
import com.mohamedfaridelsherbini.nexar.data.db.AppDatabase
import com.mohamedfaridelsherbini.nexar.data.db.getDatabaseBuilder
import com.mohamedfaridelsherbini.nexar.data.repo.DocumentRepositoryImpl
import com.mohamedfaridelsherbini.nexar.di.AppContainer
import com.mohamedfaridelsherbini.nexar.domain.usecase.initOcr
import com.mohamedfaridelsherbini.nexar.storage.AndroidStorageRepository

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

private lateinit var appContainer: AppContainer

fun initPlatform(context: Context) {
    val appContext = context.applicationContext
    com.mohamedfaridelsherbini.nexar.data.db.initDatabase(appContext)
    initOcr(appContext)
    appContainer = AppContainer(
        documentRepository = DocumentRepositoryImpl(database.documentDao()),
        storageRepository = AndroidStorageRepository(appContext)
    )
}

private val database: AppDatabase by lazy {
    getDatabaseBuilder().build()
}

internal fun provideAppContainer(): AppContainer {
    check(::appContainer.isInitialized) {
        "AppContainer was requested before initPlatform() completed."
    }
    return appContainer
}
