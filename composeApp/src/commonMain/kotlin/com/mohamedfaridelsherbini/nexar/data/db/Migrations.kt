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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE documents ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE documents ADD COLUMN extractedAmount TEXT")
        connection.execSQL("ALTER TABLE documents ADD COLUMN extractedDate TEXT")
        connection.execSQL("ALTER TABLE documents ADD COLUMN duplicateOfId TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        // Create the FTS4 virtual table for full-text search
        connection.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS documents_fts " +
                "USING fts4(documentId TEXT, name TEXT, ocrText TEXT, category TEXT, tags TEXT)"
        )
        // Backfill from existing documents.
        // tagsJson is stored as JSON ("[\"a\",\"b\"]"); the FTS tokeniser treats
        // brackets, quotes, and commas as delimiters so the actual words are indexed.
        connection.execSQL(
            "INSERT INTO documents_fts (documentId, name, ocrText, category, tags) " +
                "SELECT id, name, ocrText, category, tagsJson FROM documents"
        )
    }
}
