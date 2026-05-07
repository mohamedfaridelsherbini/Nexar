package com.mohamedfaridelsherbini.nexar.di

import com.mohamedfaridelsherbini.nexar.domain.repository.DocumentRepository
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import com.mohamedfaridelsherbini.nexar.domain.usecase.AddScannedDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.CreateFolderUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import com.mohamedfaridelsherbini.nexar.domain.usecase.DeleteDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveDocumentsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.RenameDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SaveDocumentToStorageUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SetStorageLocationUseCase

class AppContainer(
    documentRepository: DocumentRepository,
    storageRepository: StorageRepository
) {
    val dashboardUseCases = DashboardUseCases(
        observeDocuments = ObserveDocumentsUseCase(documentRepository),
        addScannedDocument = AddScannedDocumentUseCase(documentRepository),
        renameDocument = RenameDocumentUseCase(documentRepository),
        deleteDocument = DeleteDocumentUseCase(documentRepository),
        observeStorageLocation = ObserveStorageLocationUseCase(storageRepository),
        setStorageLocation = SetStorageLocationUseCase(storageRepository),
        saveDocumentToStorage = SaveDocumentToStorageUseCase(storageRepository),
        createFolder = CreateFolderUseCase(storageRepository)
    )
}

expect fun getAppContainer(): AppContainer
