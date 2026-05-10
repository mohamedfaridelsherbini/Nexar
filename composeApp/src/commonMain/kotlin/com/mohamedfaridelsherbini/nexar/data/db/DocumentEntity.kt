package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.Entity
import androidx.room.Fts3
import androidx.room.PrimaryKey
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateMillis: Long,
    val imageUrisJson: String,
    val pdfUri: String?,
    val ocrText: String = "",
    val category: String = "Other",
    val tagsJson: String = "[]",
    val isExportedToStorage: Boolean = false,
    val ocrProcessed: Boolean = false,
    val isStarred: Boolean = false,
    val extractedAmount: String? = null,
    val extractedDate: String? = null,
    val duplicateOfId: String? = null,
) {
    fun toDomain() =
        ScannedDocument(
            id = id,
            name = name,
            dateMillis = dateMillis,
            imageUris = Json.decodeFromString(imageUrisJson),
            pdfUri = pdfUri,
            ocrText = ocrText,
            category = runCatching { DocumentCategory.valueOf(category) }.getOrDefault(DocumentCategory.Other),
            tags = runCatching { Json.decodeFromString<List<String>>(tagsJson) }.getOrDefault(emptyList()),
            isExportedToStorage = isExportedToStorage,
            ocrProcessed = ocrProcessed,
            isStarred = isStarred,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            duplicateOfId = duplicateOfId,
        )

    companion object {
        fun fromDomain(doc: ScannedDocument) =
            DocumentEntity(
                id = doc.id,
                name = doc.name,
                dateMillis = doc.dateMillis,
                imageUrisJson = Json.encodeToString(doc.imageUris),
                pdfUri = doc.pdfUri,
                ocrText = doc.ocrText,
                category = doc.category.name,
                tagsJson = Json.encodeToString(doc.tags),
                isExportedToStorage = doc.isExportedToStorage,
                ocrProcessed = doc.ocrProcessed,
                isStarred = doc.isStarred,
                extractedAmount = doc.extractedAmount,
                extractedDate = doc.extractedDate,
                duplicateOfId = doc.duplicateOfId,
            )
    }
}

@Entity(tableName = "documents_fts")
@Fts3
data class DocumentFtsEntity(
    val documentId: String,
    val name: String,
    val ocrText: String,
    val category: String,
    val tags: String,
) {
    companion object {
        fun fromDomain(doc: ScannedDocument) =
            DocumentFtsEntity(
                documentId = doc.id,
                name = doc.name,
                ocrText = doc.ocrText,
                category = doc.category.displayName,
                tags = doc.tags.joinToString(" "),
            )
    }
}
