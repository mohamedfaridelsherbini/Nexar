package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import com.mohamedfaridelsherbini.nexar.domain.usecase.ProcessScannedDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SearchDocumentsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.StorageAnalyticsUseCase
import com.mohamedfaridelsherbini.nexar.platform.triggerSuccessHaptic
import com.mohamedfaridelsherbini.nexar.platform.triggerWarningHaptic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** All mutable UI signals bundled to keep [combine] readable. */
private data class UiPreferences(
    val sort: SortOrder = SortOrder.Newest,
    val searchQuery: String = "",
    val activeFilter: DashboardFilter = DashboardFilter.All,
    val ocrSheetDocumentId: String? = null,
    val batchExportResult: Pair<Int, Int>? = null,
    val hasLoaded: Boolean = false,
    val processingDocumentId: String? = null,
    val exportingDocumentId: String? = null,
    val isBatchExporting: Boolean = false,
    val error: NexarError? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val useCases: DashboardUseCases,
    private val processScannedDocument: ProcessScannedDocumentUseCase,
    private val analyticsUseCase: StorageAnalyticsUseCase,
    private val searchDocuments: SearchDocumentsUseCase
) : ViewModel() {

    private val _prefs = MutableStateFlow(UiPreferences())

    /**
     * When a search query is active, swap the documents source to the FTS-backed
     * [SearchDocumentsUseCase] flow.  [flatMapLatest] ensures that changing the
     * query cancels the previous search and starts a new one immediately.
     */
    private val documentsFlow = _prefs
        .map { it.searchQuery }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                useCases.observeDocuments()
                    .onEach { _prefs.update { it.copy(hasLoaded = true) } }
            } else {
                searchDocuments(query)
                    .onEach { _prefs.update { it.copy(hasLoaded = true) } }
            }
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        documentsFlow,
        useCases.observeStorageLocation(),
        _prefs
    ) { documents, storageLocation, prefs ->
        val sorted = documents.sorted(prefs.sort)
        // Category/star filtering still applied on top of FTS results
        val visible = sorted.applyFilter(prefs.activeFilter)
        DashboardUiState(
            documents = sorted,
            visibleDocuments = visible,
            storageLocation = storageLocation,
            sort = prefs.sort,
            batchExportResult = prefs.batchExportResult,
            analytics = analyticsUseCase(sorted),
            searchQuery = prefs.searchQuery,
            activeFilter = prefs.activeFilter,
            ocrSheetDocumentId = prefs.ocrSheetDocumentId,
            isLoadingDocuments = !prefs.hasLoaded,
            processingDocumentId = prefs.processingDocumentId,
            exportingDocumentId = prefs.exportingDocumentId,
            isBatchExporting = prefs.isBatchExporting,
            error = prefs.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    // ── Scan ──────────────────────────────────────────────────────────────────

    fun onDocumentScanned(document: ScannedDocument) {
        viewModelScope.launch {
            _prefs.update { it.copy(processingDocumentId = document.id) }
            try {
                useCases.addScannedDocument(document)
                triggerSuccessHaptic()
                val isDuplicate = processScannedDocument(document)
                if (isDuplicate) triggerWarningHaptic()
            } catch (e: Exception) {
                _prefs.update { it.copy(error = NexarError.OcrFailed(document.name)) }
            } finally {
                _prefs.update { it.copy(processingDocumentId = null) }
            }
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun onSaveDocumentUpdate(document: ScannedDocument) {
        viewModelScope.launch { useCases.updateDocument(document) }
    }

    fun onRenameDocument(documentId: String, newName: String) {
        viewModelScope.launch { useCases.renameDocument(documentId, newName) }
    }

    fun onDeleteDocument(document: ScannedDocument) {
        viewModelScope.launch { useCases.deleteDocument(document) }
    }

    fun onToggleStar(document: ScannedDocument) {
        viewModelScope.launch { useCases.toggleStar(document) }
    }

    // ── Storage ───────────────────────────────────────────────────────────────

    fun onStorageLocationSelected(uri: String) {
        useCases.setStorageLocation(uri)
    }

    fun onSaveDocumentToStorage(document: ScannedDocument) {
        viewModelScope.launch {
            _prefs.update { it.copy(exportingDocumentId = document.id) }
            try {
                val success = useCases.saveDocumentToStorage(document)
                if (success) {
                    triggerSuccessHaptic()
                } else {
                    _prefs.update { it.copy(error = NexarError.ExportFailed(document.name)) }
                }
            } catch (e: Exception) {
                _prefs.update { it.copy(error = NexarError.ExportFailed(document.name)) }
            } finally {
                _prefs.update { it.copy(exportingDocumentId = null) }
            }
        }
    }

    fun onBatchExport() {
        viewModelScope.launch {
            _prefs.update { it.copy(isBatchExporting = true) }
            try {
                val docs = uiState.value.documents
                val result = useCases.batchExport(docs)
                _prefs.update { it.copy(batchExportResult = result) }
                if (result.first > 0) triggerSuccessHaptic()
            } catch (e: Exception) {
                _prefs.update { it.copy(error = NexarError.ExportFailed("batch export")) }
            } finally {
                _prefs.update { it.copy(isBatchExporting = false) }
            }
        }
    }

    fun onBatchResultDismissed() {
        _prefs.update { it.copy(batchExportResult = null) }
    }

    fun onCreateFolder(folderName: String) {
        viewModelScope.launch {
            try {
                val success = useCases.createFolder(folderName)
                if (!success) _prefs.update { it.copy(error = NexarError.FolderCreationFailed) }
            } catch (e: Exception) {
                _prefs.update { it.copy(error = NexarError.FolderCreationFailed) }
            }
        }
    }

    // ── UI-state mutations ────────────────────────────────────────────────────

    fun onSortChanged(order: SortOrder) {
        _prefs.update { it.copy(sort = order) }
    }

    fun onSearchQueryChanged(query: String) {
        _prefs.update { it.copy(searchQuery = query) }
    }

    fun onFilterChanged(filter: DashboardFilter) {
        _prefs.update { it.copy(activeFilter = filter) }
    }

    fun onOcrSheetOpen(documentId: String) {
        _prefs.update { it.copy(ocrSheetDocumentId = documentId) }
    }

    fun onOcrSheetDismissed() {
        _prefs.update { it.copy(ocrSheetDocumentId = null) }
    }

    fun onErrorDismissed() {
        _prefs.update { it.copy(error = null) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun List<ScannedDocument>.sorted(order: SortOrder) = when (order) {
        SortOrder.Newest -> sortedByDescending { it.dateMillis }
        SortOrder.Oldest -> sortedBy { it.dateMillis }
        SortOrder.NameAsc -> sortedBy { it.name.lowercase() }
        SortOrder.CategoryAsc -> sortedBy { it.category.displayName }
    }

    private fun List<ScannedDocument>.applyFilter(filter: DashboardFilter) = when (filter) {
        DashboardFilter.All -> this
        DashboardFilter.NeedsExport -> filter { !it.isExportedToStorage }
        DashboardFilter.Starred -> filter { it.isStarred }
        else -> filter.toCategory()?.let { cat -> filter { it.category == cat } } ?: this
    }
}
