package com.mohamedfaridelsherbini.nexar.presentation.dashboard

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

enum class DashboardFilter {
    All,
    NeedsExport,
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
        Receipt -> "Receipts"
        Invoice -> "Invoices"
        IdDocument -> "IDs"
        Contract -> "Contracts"
        Medical -> "Medical"
    }
}

data class DashboardUiState(
    val documents: List<ScannedDocument> = emptyList(),
    val storageLocation: String? = null
)
