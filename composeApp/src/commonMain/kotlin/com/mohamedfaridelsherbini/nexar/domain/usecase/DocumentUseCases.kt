package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow

class ObserveDocumentsUseCase(
    private val documentRepository: DocumentRepository
) {
    operator fun invoke(): Flow<List<ScannedDocument>> = documentRepository.observeDocuments()
}

class AddScannedDocumentUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.saveDocument(document)
    }
}

class RenameDocumentUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(documentId: String, newName: String) {
        val sanitizedName = newName.trim()
        if (sanitizedName.isBlank()) return
        documentRepository.renameDocument(documentId, sanitizedName)
    }
}

class DeleteDocumentUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.deleteDocument(document)
    }
}

class UpdateDocumentUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(document: ScannedDocument) {
        documentRepository.updateDocument(document)
    }
}

class MarkExportedUseCase(
    private val documentRepository: DocumentRepository
) {
    suspend operator fun invoke(id: String) {
        documentRepository.markExported(id)
    }
}
