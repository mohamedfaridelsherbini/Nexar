package com.mohamedfaridelsherbini.nexar

import com.mohamedfaridelsherbini.nexar.data.db.AppDatabase
import com.mohamedfaridelsherbini.nexar.data.db.getDatabaseBuilder
import com.mohamedfaridelsherbini.nexar.data.repo.DocumentRepositoryImpl
import com.mohamedfaridelsherbini.nexar.di.AppContainer
import com.mohamedfaridelsherbini.nexar.storage.IOSStorageRepository

class IOSPlatform: Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform()

private val database: AppDatabase by lazy {
    getDatabaseBuilder().build()
}

private val appContainer: AppContainer by lazy {
    AppContainer(
        documentRepository = DocumentRepositoryImpl(database.documentDao()),
        storageRepository = IOSStorageRepository()
    )
}

internal fun provideAppContainer(): AppContainer = appContainer
