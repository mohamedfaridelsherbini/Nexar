package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val useCases: DashboardUseCases
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> =
        combine(
            useCases.observeDocuments(),
            useCases.observeStorageLocation()
        ) { documents, storageLocation ->
            DashboardUiState(
                documents = documents,
                storageLocation = storageLocation
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    fun onDocumentScanned(document: ScannedDocument) {
        viewModelScope.launch {
            useCases.addScannedDocument(document)
        }
    }

    fun onRenameDocument(documentId: String, newName: String) {
        viewModelScope.launch {
            useCases.renameDocument(documentId, newName)
        }
    }

    fun onDeleteDocument(document: ScannedDocument) {
        viewModelScope.launch {
            useCases.deleteDocument(document)
        }
    }

    fun onStorageLocationSelected(uri: String) {
        useCases.setStorageLocation(uri)
    }

    fun onSaveDocumentToStorage(document: ScannedDocument) {
        viewModelScope.launch {
            useCases.saveDocumentToStorage(document)
        }
    }

    fun onCreateFolder(folderName: String) {
        viewModelScope.launch {
            useCases.createFolder(folderName)
        }
    }
}
