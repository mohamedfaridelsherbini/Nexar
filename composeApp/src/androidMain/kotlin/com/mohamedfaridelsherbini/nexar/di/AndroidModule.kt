package com.mohamedfaridelsherbini.nexar.di

import com.mohamedfaridelsherbini.nexar.data.db.AppDatabase
import com.mohamedfaridelsherbini.nexar.data.db.getDatabaseBuilder
import com.mohamedfaridelsherbini.nexar.data.repo.DocumentRepositoryImpl
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import com.mohamedfaridelsherbini.nexar.storage.AndroidStorageRepository
import com.mohamedfaridelsherbini.nexar.platform.initHaptic
import com.mohamedfaridelsherbini.nexar.platform.initNexarNotifier
import com.mohamedfaridelsherbini.nexar.platform.initNexarPrefs
import com.mohamedfaridelsherbini.nexar.platform.initShare
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule =
    module {
        single<AppDatabase> {
            val context = androidContext()
            initNexarPrefs(context)
            initHaptic(context)
            initShare(context)
            initNexarNotifier(context)
            getDatabaseBuilder().build()
        }
        single<DocumentRepository> {
            val db = get<AppDatabase>()
            DocumentRepositoryImpl(db.documentDao(), db.documentFtsDao())
        }
        single<StorageRepository> { AndroidStorageRepository(androidContext()) }
    }
