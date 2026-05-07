package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentClassifier
import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentNamer
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import com.mohamedfaridelsherbini.nexar.domain.usecase.OcrProcessor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val useCases: DashboardUseCases,
    private val ocrProcessor: OcrProcessor
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
            runOcrAndClassify(document)
        }
    }

    private suspend fun runOcrAndClassify(document: ScannedDocument) {
        if (document.imageUris.isEmpty()) return
        val ocrText = ocrProcessor.extractText(document.imageUris)
        val category = DocumentClassifier.classify(ocrText)
        val tags = DocumentClassifier.extractTags(ocrText)
        val suggestedName = if (!document.ocrProcessed) {
            DocumentNamer.suggest(ocrText, category, document.dateMillis) ?: document.name
        } else {
            document.name
        }
        val updated = document.copy(
            ocrText = ocrText,
            category = category,
            tags = tags,
            name = suggestedName,
            ocrProcessed = true
        )
        useCases.updateDocument(updated)
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
