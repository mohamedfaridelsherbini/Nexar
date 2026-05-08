package com.mohamedfaridelsherbini.nexar.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScannedDocument(
    val id: String,
    val name: String,
    val dateMillis: Long,
    val imageUris: List<String>,
    val pdfUri: String? = null,
    val ocrText: String = "",
    val category: DocumentCategory = DocumentCategory.Other,
    val tags: List<String> = emptyList(),
    val isExportedToStorage: Boolean = false,
    /** True when OCR has been attempted (whether or not text was found). */
    val ocrProcessed: Boolean = false,
    val isStarred: Boolean = false,
    val extractedAmount: String? = null,
    val extractedDate: String? = null,
    val duplicateOfId: String? = null
)
