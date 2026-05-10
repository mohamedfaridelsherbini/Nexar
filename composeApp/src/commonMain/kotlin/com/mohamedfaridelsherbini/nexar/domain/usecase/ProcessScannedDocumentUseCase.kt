package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.classifier.ClassifierService
import com.mohamedfaridelsherbini.nexar.domain.classifier.DuplicateDetectionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.ExtractionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.NamingService
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.first

/**
 * Orchestrates the full post-scan intelligence pipeline:
 * OCR → classify → tag → name → extract amount/date → detect duplicate → persist.
 *
 * Returns `true` when a probable duplicate was found (caller may trigger a haptic warning).
 */
class ProcessScannedDocumentUseCase(
    private val ocrProcessor: OcrProcessor,
    private val classifier: ClassifierService,
    private val namer: NamingService,
    private val extractor: ExtractionService,
    private val duplicateDetector: DuplicateDetectionService,
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument): Boolean {
        if (document.imageUris.isEmpty()) return false

        val ocrText = ocrProcessor.extractText(document.imageUris)
        val category = classifier.classify(ocrText)
        val tags = classifier.extractTags(ocrText)
        val suggestedName =
            if (!document.ocrProcessed) {
                namer.suggest(ocrText, category, document.dateMillis) ?: document.name
            } else {
                document.name
            }
        val amount = extractor.extractAmount(ocrText)
        val date = extractor.extractDate(ocrText)

        // Fresh list from repository (excludes the just-saved doc by id) to avoid stale-read race
        val existingDocs =
            documentRepository.observeDocuments().first()
                .filter { it.id != document.id }
        val duplicateId =
            duplicateDetector.findDuplicate(
                document.copy(ocrText = ocrText),
                existingDocs,
            )

        documentRepository.updateDocument(
            document.copy(
                ocrText = ocrText,
                category = category,
                tags = tags,
                name = suggestedName,
                ocrProcessed = true,
                extractedAmount = amount,
                extractedDate = date,
                duplicateOfId = duplicateId,
            ),
        )
        return duplicateId != null
    }
}
