package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import kotlin.test.Test
import kotlin.test.assertEquals

class StorageAnalyticsUseCaseTest {
    private fun doc(
        id: String,
        category: DocumentCategory,
        pageCount: Int,
        exported: Boolean = false,
    ) = ScannedDocument(
        id = id,
        name = "Doc $id",
        dateMillis = 0L,
        imageUris = List(pageCount) { "img-$it.jpg" },
        category = category,
        isExportedToStorage = exported,
    )

    @Test
    fun `storage analytics aggregates totals per category and exported count`() {
        val result = StorageAnalyticsUseCase()(
            listOf(
                doc("1", DocumentCategory.Invoice, pageCount = 2, exported = true),
                doc("2", DocumentCategory.Receipt, pageCount = 1),
                doc("3", DocumentCategory.Invoice, pageCount = 3),
            )
        )

        assertEquals(2_400_000L, result.totalBytes)
        assertEquals(3, result.totalDocuments)
        assertEquals(1, result.exportedDocuments)
        assertEquals(2_000_000L, result.perCategory.getValue(DocumentCategory.Invoice))
        assertEquals(400_000L, result.perCategory.getValue(DocumentCategory.Receipt))
    }

    @Test
    fun `storage analytics treats zero page documents as one page`() {
        val result = StorageAnalyticsUseCase()(
            listOf(
                ScannedDocument(
                    id = "1",
                    name = "Doc",
                    dateMillis = 0L,
                    imageUris = emptyList(),
                )
            )
        )

        assertEquals(400_000L, result.totalBytes)
        assertEquals(400_000L, result.perCategory.getValue(DocumentCategory.Other))
    }
}
