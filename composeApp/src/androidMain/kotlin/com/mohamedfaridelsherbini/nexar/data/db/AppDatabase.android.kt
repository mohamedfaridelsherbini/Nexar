package com.mohamedfaridelsherbini.nexar.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

import org.koin.core.context.GlobalContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = GlobalContext.get().get<Context>()
    val dbFile = context.getDatabasePath("nexar_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .fallbackToDestructiveMigration(true)
}
