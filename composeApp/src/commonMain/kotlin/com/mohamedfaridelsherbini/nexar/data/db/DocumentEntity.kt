package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.Entity
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
    val ocrProcessed: Boolean = false
) {
    fun toDomain() = ScannedDocument(
        id = id,
        name = name,
        dateMillis = dateMillis,
        imageUris = Json.decodeFromString(imageUrisJson),
        pdfUri = pdfUri,
        ocrText = ocrText,
        category = runCatching { DocumentCategory.valueOf(category) }.getOrDefault(DocumentCategory.Other),
        tags = runCatching { Json.decodeFromString<List<String>>(tagsJson) }.getOrDefault(emptyList()),
        isExportedToStorage = isExportedToStorage,
        ocrProcessed = ocrProcessed
    )

    companion object {
        fun fromDomain(doc: ScannedDocument) = DocumentEntity(
            id = doc.id,
            name = doc.name,
            dateMillis = doc.dateMillis,
            imageUrisJson = Json.encodeToString(doc.imageUris),
            pdfUri = doc.pdfUri,
            ocrText = doc.ocrText,
            category = doc.category.name,
            tagsJson = Json.encodeToString(doc.tags),
            isExportedToStorage = doc.isExportedToStorage,
            ocrProcessed = doc.ocrProcessed
        )
    }
}
