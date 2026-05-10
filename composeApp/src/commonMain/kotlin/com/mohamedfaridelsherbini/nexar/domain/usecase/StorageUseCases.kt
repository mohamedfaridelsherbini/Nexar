package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow

class ObserveStorageLocationUseCase(
    private val storageRepository: StorageRepository,
) {
    operator fun invoke(): Flow<String?> = storageRepository.observeStorageLocation()
}

class SetStorageLocationUseCase(
    private val storageRepository: StorageRepository,
) {
    operator fun invoke(uri: String) {
        val sanitizedUri = uri.trim()
        if (sanitizedUri.isBlank()) return
        storageRepository.setStorageLocation(sanitizedUri)
    }
}

class SaveDocumentToStorageUseCase(
    private val storageRepository: StorageRepository,
    private val documentRepository: DocumentRepository,
) {
    suspend operator fun invoke(document: ScannedDocument): Boolean {
        if (document.pdfUri == null) return false
        val success = storageRepository.saveDocument(document)
        if (success) documentRepository.markExported(document.id)
        return success
    }
}

class CreateFolderUseCase(
    private val storageRepository: StorageRepository,
) {
    suspend operator fun invoke(folderName: String): Boolean {
        val sanitizedName = folderName.trim()
        if (sanitizedName.isBlank()) return false
        return storageRepository.createFolder(sanitizedName)
    }
}

class BatchExportUseCase(
    private val storageRepository: StorageRepository,
    private val documentRepository: DocumentRepository,
) {
    /** Returns Pair(successCount, failedCount). */
    suspend operator fun invoke(documents: List<ScannedDocument>): Pair<Int, Int> {
        val unexported = documents.filter { !it.isExportedToStorage && it.pdfUri != null }
        var success = 0
        var failed = 0
        for (doc in unexported) {
            val ok = storageRepository.saveDocument(doc)
            if (ok) {
                documentRepository.markExported(doc.id)
                success++
            } else {
                failed++
            }
        }
        return success to failed
    }
}

/**
 * Convenience bundle scoped to [DashboardViewModel].
 *
 * Intentionally wide: it aggregates every operation that the dashboard orchestrates.
 * If additional screens are added that need only a subset of these operations, inject
 * the individual use-case classes directly instead of widening this bundle.
 */
data class DashboardUseCases(
    val observeDocuments: ObserveDocumentsUseCase,
    val addScannedDocument: AddScannedDocumentUseCase,
    val updateDocument: UpdateDocumentUseCase,
    val renameDocument: RenameDocumentUseCase,
    val deleteDocument: DeleteDocumentUseCase,
    val markExported: MarkExportedUseCase,
    val toggleStar: ToggleStarUseCase,
    val observeStorageLocation: ObserveStorageLocationUseCase,
    val setStorageLocation: SetStorageLocationUseCase,
    val saveDocumentToStorage: SaveDocumentToStorageUseCase,
    val batchExport: BatchExportUseCase,
    val createFolder: CreateFolderUseCase,
)
