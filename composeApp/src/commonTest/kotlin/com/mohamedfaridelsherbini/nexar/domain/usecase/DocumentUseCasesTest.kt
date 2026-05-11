package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.fakes.FakeDocumentRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class DocumentUseCasesTest {
    private fun doc(
        id: String = "1",
        name: String = "Doc",
        isStarred: Boolean = false,
        trashedAtMillis: Long? = null,
        tags: List<String> = emptyList(),
        ocrText: String = "",
    ) = ScannedDocument(
        id = id,
        name = name,
        dateMillis = 0L,
        imageUris = listOf("img.jpg"),
        category = DocumentCategory.Other,
        isStarred = isStarred,
        trashedAtMillis = trashedAtMillis,
        tags = tags,
        ocrText = ocrText,
    )

    @Test
    fun `add scanned document saves document`() = runTest {
        val repo = FakeDocumentRepository()

        AddScannedDocumentUseCase(repo)(doc("1"))

        assertEquals(listOf("1"), repo.saved.map { it.id })
    }

    @Test
    fun `rename document trims name before saving`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(listOf(doc("1", "Old Name")))

        RenameDocumentUseCase(repo)("1", "  New Name  ")

        assertEquals("New Name", repo.observeDocuments().first().single().name)
    }

    @Test
    fun `rename document ignores blank names`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(listOf(doc("1", "Old Name")))

        RenameDocumentUseCase(repo)("1", "   ")

        assertEquals("Old Name", repo.observeDocuments().first().single().name)
    }

    @Test
    fun `delete and restore document moves it between active and trash flows`() = runTest {
        val repo = FakeDocumentRepository()
        val target = doc("1")
        repo.setDocuments(listOf(target))

        DeleteDocumentUseCase(repo)(target)
        assertTrue(repo.observeDocuments().first().isEmpty())
        assertEquals(listOf("1"), ObserveTrashedDocumentsUseCase(repo)().first().map { it.id })

        RestoreDocumentUseCase(repo)("1")
        assertEquals(listOf("1"), ObserveDocumentsUseCase(repo)().first().map { it.id })
        assertTrue(repo.observeTrashedDocuments().first().isEmpty())
    }

    @Test
    fun `permanently delete removes document entirely`() = runTest {
        val repo = FakeDocumentRepository()
        val target = doc("1", trashedAtMillis = 1L)
        repo.setDocuments(listOf(target))

        PermanentlyDeleteDocumentUseCase(repo)(target)

        assertTrue(repo.observeDocuments().first().isEmpty())
        assertTrue(repo.observeTrashedDocuments().first().isEmpty())
    }

    @Test
    fun `update document replaces repository value`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(listOf(doc("1", "Original")))

        UpdateDocumentUseCase(repo)(doc("1", "Updated"))

        assertEquals("Updated", repo.observeDocuments().first().single().name)
    }

    @Test
    fun `mark exported updates repository flag`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(listOf(doc("1")))

        MarkExportedUseCase(repo)("1")

        assertTrue(repo.observeDocuments().first().single().isExportedToStorage)
    }

    @Test
    fun `toggle star flips starred flag`() = runTest {
        val repo = FakeDocumentRepository()
        val target = doc("1", isStarred = false)

        ToggleStarUseCase(repo)(target)

        assertEquals("1", repo.lastUpdated?.id)
        assertTrue(repo.lastUpdated?.isStarred == true)
    }

    @Test
    fun `search documents returns empty flow for blank query`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(listOf(doc("1", "Invoice")))

        val result = SearchDocumentsUseCase(repo)("   ").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search documents sanitizes tokens for FTS prefix matching`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(
            listOf(
                doc("1", "Invoice January", tags = listOf("urgent")),
                doc("2", "Receipt", ocrText = "invoice copy"),
            )
        )
        val useCase = SearchDocumentsUseCase(repo)

        assertEquals("inv* urgent*", useCase.sanitizeFtsQuery(" inv- \"urgent\" "))
        assertEquals(listOf("1", "2"), useCase("inv").first().map { it.id })
    }

    @Test
    fun `search documents excludes trashed matches`() = runTest {
        val repo = FakeDocumentRepository()
        repo.setDocuments(
            listOf(
                doc("1", "Invoice", trashedAtMillis = 10L),
                doc("2", "Invoice Active"),
            )
        )

        val result = SearchDocumentsUseCase(repo)("invoice").first()

        assertEquals(listOf("2"), result.map { it.id })
        assertFalse(result.any { it.id == "1" })
    }
}
