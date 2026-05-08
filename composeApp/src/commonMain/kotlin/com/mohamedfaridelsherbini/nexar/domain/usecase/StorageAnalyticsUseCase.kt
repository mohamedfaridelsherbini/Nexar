package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument

data class StorageAnalytics(
    val totalBytes: Long,
    val perCategory: Map<DocumentCategory, Long>,
    val totalDocuments: Int,
    val exportedDocuments: Int
)

/**
 * Computes storage analytics from a list of documents.
 * File size is estimated at 400 KB per page when the actual size is unavailable,
 * since we cannot access file system sizes from KMP common code without expect/actual plumbing.
 */
class StorageAnalyticsUseCase {
    private val bytesPerPage = 400_000L

    operator fun invoke(documents: List<ScannedDocument>): StorageAnalytics {
        var total = 0L
        val perCat = mutableMapOf<DocumentCategory, Long>()

        for (doc in documents) {
            val size = doc.imageUris.size.coerceAtLeast(1) * bytesPerPage
            total += size
            perCat[doc.category] = (perCat[doc.category] ?: 0L) + size
        }

        return StorageAnalytics(
            totalBytes = total,
            perCategory = perCat,
            totalDocuments = documents.size,
            exportedDocuments = documents.count { it.isExportedToStorage }
        )
    }
}

