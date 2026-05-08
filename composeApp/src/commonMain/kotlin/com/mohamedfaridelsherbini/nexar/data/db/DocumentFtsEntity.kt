package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.Entity
import androidx.room.Fts4
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

/**
 * FTS4 virtual table that mirrors the searchable text fields of [DocumentEntity].
 *
 * The table is kept in sync manually (delete + insert) by [DocumentRepositoryImpl]
 * on every write operation. Room tracks invalidation for virtual tables, so
 * a JOIN-based Flow in [DocumentDao] re-emits correctly when either table changes.
 */
@Entity(tableName = "documents_fts")
@Fts4
data class DocumentFtsEntity(
    /** Foreign key equivalent — links to [DocumentEntity.id]. */
    val documentId: String,
    val name: String,
    val ocrText: String,
    /** Human-readable category label (e.g. "Invoice", "Receipt"). */
    val category: String,
    /** Space-separated tags for better tokenisation (FTS splits on whitespace). */
    val tags: String
) {
    companion object {
        fun fromDomain(doc: ScannedDocument) = DocumentFtsEntity(
            documentId = doc.id,
            name = doc.name,
            ocrText = doc.ocrText,
            category = doc.category.displayName,
            tags = doc.tags.joinToString(" ")
        )
    }
}
