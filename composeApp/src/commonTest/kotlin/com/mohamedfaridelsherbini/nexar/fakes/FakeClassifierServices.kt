package com.mohamedfaridelsherbini.nexar.fakes

import com.mohamedfaridelsherbini.nexar.domain.classifier.ClassifierService
import com.mohamedfaridelsherbini.nexar.domain.classifier.DuplicateDetectionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.ExtractionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.NamingService
import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

class FakeClassifierService(
    private val category: DocumentCategory = DocumentCategory.Other,
    private val tags: List<String> = emptyList(),
) : ClassifierService {
    override fun classify(ocrText: String): DocumentCategory = category

    override fun extractTags(ocrText: String): List<String> = tags
}

class FakeNamingService(private val name: String? = null) : NamingService {
    override fun suggest(
        ocrText: String,
        category: DocumentCategory,
        dateMillis: Long,
    ): String? = name
}

class FakeExtractionService(
    private val amount: String? = null,
    private val date: String? = null,
) : ExtractionService {
    override fun extractAmount(ocrText: String): String? = amount

    override fun extractDate(ocrText: String): String? = date
}

class FakeDuplicateDetectionService(
    private val duplicateId: String? = null,
) : DuplicateDetectionService {
    override fun findDuplicate(
        newDoc: ScannedDocument,
        existingDocs: List<ScannedDocument>,
    ): String? = duplicateId
}
