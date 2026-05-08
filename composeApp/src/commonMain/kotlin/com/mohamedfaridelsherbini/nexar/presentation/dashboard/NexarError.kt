package com.mohamedfaridelsherbini.nexar.presentation.dashboard

sealed class NexarError {
    /** OCR or classification failed for [documentName]. */
    data class OcrFailed(val documentName: String) : NexarError()

    /** Saving a document to the export folder failed. */
    data class ExportFailed(val documentName: String) : NexarError()

    /** Creating a new folder in the export location failed. */
    data object FolderCreationFailed : NexarError()

    fun userMessage(): String = when (this) {
        is OcrFailed -> "Could not analyse \"$documentName\". The document was saved but OCR failed."
        is ExportFailed -> "Failed to export \"$documentName\". Check storage permission and try again."
        FolderCreationFailed -> "Could not create the folder. Check storage permission and try again."
    }
}
