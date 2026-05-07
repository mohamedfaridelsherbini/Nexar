package com.mohamedfaridelsherbini.nexar.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateMillis: Long,
    val imageUrisJson: String,
    val pdfUri: String?
) {
    fun toDomain() = ScannedDocument(
        id = id,
        name = name,
        dateMillis = dateMillis,
        imageUris = Json.decodeFromString(imageUrisJson),
        pdfUri = pdfUri
    )

    companion object {
        fun fromDomain(doc: ScannedDocument) = DocumentEntity(
            id = doc.id,
            name = doc.name,
            dateMillis = doc.dateMillis,
            imageUrisJson = Json.encodeToString(doc.imageUris),
            pdfUri = doc.pdfUri
        )
    }
}
