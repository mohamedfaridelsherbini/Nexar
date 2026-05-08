package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.usecase.StorageAnalytics

enum class DashboardFilter {
    All,
    NeedsExport,
    Starred,
    Receipt,
    Invoice,
    IdDocument,
    Contract,
    Medical;

    fun toCategory(): DocumentCategory? = when (this) {
        Receipt -> DocumentCategory.Receipt
        Invoice -> DocumentCategory.Invoice
        IdDocument -> DocumentCategory.IdDocument
        Contract -> DocumentCategory.Contract
        Medical -> DocumentCategory.Medical
        else -> null
    }

    val label: String get() = when (this) {
        All -> "All"
        NeedsExport -> "Needs export"
        Starred -> "Starred"
        Receipt -> "Receipts"
        Invoice -> "Invoices"
        IdDocument -> "IDs"
        Contract -> "Contracts"
        Medical -> "Medical"
    }
}

data class DashboardUiState(
    /** All documents, sorted per [sort]. */
    val documents: List<ScannedDocument> = emptyList(),
    /** Derived from [documents], [searchQuery], and [activeFilter]. Owned by ViewModel. */
    val visibleDocuments: List<ScannedDocument> = emptyList(),
    val storageLocation: String? = null,
    val sort: SortOrder = SortOrder.Newest,
    val batchExportResult: Pair<Int, Int>? = null,
    val analytics: StorageAnalytics = StorageAnalytics(0L, emptyMap(), 0, 0),
    // UI-driven state — owned by ViewModel so previews and tests are trivial
    val searchQuery: String = "",
    val activeFilter: DashboardFilter = DashboardFilter.All,
    val ocrSheetDocumentId: String? = null,
    // ── Loading & error ───────────────────────────────────────────────────────
    /** True until the first emission from the document repository arrives. */
    val isLoadingDocuments: Boolean = true,
    /** Id of the document currently being OCR-processed. Null when idle. */
    val processingDocumentId: String? = null,
    /** Id of the document currently being exported to storage. Null when idle. */
    val exportingDocumentId: String? = null,
    /** True while a batch export is running. */
    val isBatchExporting: Boolean = false,
    /** Non-null when an operation failed. Cleared by [DashboardViewModel.onErrorDismissed]. */
    val error: NexarError? = null,
)
