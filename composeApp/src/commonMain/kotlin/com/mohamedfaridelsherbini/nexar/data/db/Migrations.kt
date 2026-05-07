package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE documents ADD COLUMN ocrText TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE documents ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'")
        connection.execSQL("ALTER TABLE documents ADD COLUMN tagsJson TEXT NOT NULL DEFAULT '[]'")
        connection.execSQL("ALTER TABLE documents ADD COLUMN isExportedToStorage INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE documents ADD COLUMN ocrProcessed INTEGER NOT NULL DEFAULT 0")
    }
}
