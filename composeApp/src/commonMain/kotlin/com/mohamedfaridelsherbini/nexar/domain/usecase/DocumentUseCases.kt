package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ObserveDocumentsUseCase(
    private val documentRepository: DocumentRepository,
) {
    operator fun invoke(): Flow<List<ScannedDocument>> = documentRepository.observeDocuments()
}

class AddScannedDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.saveDocument(document)
    }
}

class RenameDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(
        documentId: String,
        newName: String,
    ) {
        val sanitizedName = newName.trim()
        if (sanitizedName.isBlank()) return
        documentRepository.renameDocument(documentId, sanitizedName)
    }
}

class DeleteDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.deleteDocument(document)
    }
}

class ObserveTrashedDocumentsUseCase(
    private val documentRepository: DocumentRepository,
) {
    operator fun invoke(): Flow<List<ScannedDocument>> = documentRepository.observeTrashedDocuments()
}

class RestoreDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(documentId: String) {
        documentRepository.restoreDocument(documentId)
    }
}

class PermanentlyDeleteDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.permanentlyDeleteDocument(document)
    }
}

class UpdateDocumentUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.updateDocument(document)
    }
}

class MarkExportedUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(id: String) {
        documentRepository.markExported(id)
    }
}

class ToggleStarUseCase(
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.updateDocument(document.copy(isStarred = !document.isStarred))
    }
}

/**
 * Wraps FTS4-backed search with query sanitization.
 *
 * Converts a raw user query (e.g. `"inv jan"`) into an FTS MATCH prefix query
 * (`"inv* jan*"`), removing characters that would otherwise break the MATCH parser.
 * Returns an empty Flow for blank queries so callers never need to guard against it.
 */
class SearchDocumentsUseCase(
    private val documentRepository: DocumentRepository,
) {
    operator fun invoke(query: String): Flow<List<ScannedDocument>> {
        if (query.isBlank()) return flowOf(emptyList())
        return documentRepository.searchDocuments(sanitizeFtsQuery(query))
    }

    internal fun sanitizeFtsQuery(query: String): String =
        query.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            // Strip FTS special chars, then append * for prefix matching
            .joinToString(" ") { token ->
                token.replace(Regex("[\"'()\\-]"), "") + "*"
            }
}
