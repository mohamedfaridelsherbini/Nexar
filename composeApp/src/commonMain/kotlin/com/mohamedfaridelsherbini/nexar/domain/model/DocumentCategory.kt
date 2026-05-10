package com.mohamedfaridelsherbini.nexar.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DocumentCategory(
    val folderName: String,
    val displayName: String,
) {
    Receipt("Receipts", "Receipt"),
    Invoice("Invoices", "Invoice"),
    IdDocument("IDs", "ID"),
    Contract("Contracts", "Contract"),
    Medical("Medical", "Medical"),
    Other("Other", "Other"),
}
