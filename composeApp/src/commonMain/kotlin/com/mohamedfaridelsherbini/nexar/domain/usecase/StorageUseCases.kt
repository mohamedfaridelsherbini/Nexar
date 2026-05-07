package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow

class ObserveStorageLocationUseCase(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<String?> = storageRepository.observeStorageLocation()
}

class SetStorageLocationUseCase(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(uri: String) {
        val sanitizedUri = uri.trim()
        if (sanitizedUri.isBlank()) return
        storageRepository.setStorageLocation(sanitizedUri)
    }
}

class SaveDocumentToStorageUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(document: ScannedDocument): Boolean {
        val sourceUri = document.pdfUri ?: return false
        return storageRepository.saveDocument(
            fileName = document.exportFileName(),
            sourceUri = sourceUri
        )
    }

    private fun ScannedDocument.exportFileName(): String {
        val sanitizedName = name.trim().ifBlank { "Document" }
        return if (sanitizedName.endsWith(".pdf", ignoreCase = true)) {
            sanitizedName
        } else {
            "$sanitizedName.pdf"
        }
    }
}

class CreateFolderUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(folderName: String): Boolean {
        val sanitizedName = folderName.trim()
        if (sanitizedName.isBlank()) return false
        return storageRepository.createFolder(sanitizedName)
    }
}

data class DashboardUseCases(
    val observeDocuments: ObserveDocumentsUseCase,
    val addScannedDocument: AddScannedDocumentUseCase,
    val renameDocument: RenameDocumentUseCase,
    val deleteDocument: DeleteDocumentUseCase,
    val observeStorageLocation: ObserveStorageLocationUseCase,
    val setStorageLocation: SetStorageLocationUseCase,
    val saveDocumentToStorage: SaveDocumentToStorageUseCase,
    val createFolder: CreateFolderUseCase
)
