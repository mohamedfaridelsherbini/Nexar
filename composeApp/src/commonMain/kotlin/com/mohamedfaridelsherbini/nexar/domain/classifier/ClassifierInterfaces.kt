package com.mohamedfaridelsherbini.nexar.domain.classifier

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

interface ClassifierService {
    fun classify(ocrText: String): DocumentCategory

    fun extractTags(ocrText: String): List<String>
}

interface NamingService {
    fun suggest(
        ocrText: String,
        category: DocumentCategory,
        dateMillis: Long,
    ): String?
}

interface ExtractionService {
    fun extractAmount(ocrText: String): String?

    fun extractDate(ocrText: String): String?
}

interface DuplicateDetectionService {
    fun findDuplicate(
        newDoc: ScannedDocument,
        existingDocs: List<ScannedDocument>,
    ): String?
}
