package com.mohamedfaridelsherbini.nexar.domain.usecase

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.repository.StorageRepository
import com.mohamedfaridelsherbini.nexar.fakes.FakeDocumentRepository
import com.mohamedfaridelsherbini.nexar.fakes.FakeStorageRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class StorageUseCasesTest {
    private fun doc(
        id: String,
        exported: Boolean = false,
        pdfUri: String? = "file:///doc.pdf",
    ) = ScannedDocument(
        id = id,
        name = "Doc $id",
        dateMillis = 0L,
        imageUris = listOf("img.jpg"),
        pdfUri = pdfUri,
        category = DocumentCategory.Other,
        isExportedToStorage = exported,
    )

    @Test
    fun `observe storage location emits current value`() = runTest {
        val repo = FakeStorageRepository().apply { currentLocation = "content://exports" }

        val result = ObserveStorageLocationUseCase(repo)().first()

        assertEquals("content://exports", result)
    }

    @Test
    fun `set storage location trims uri`() {
        val repo = FakeStorageRepository()

        SetStorageLocationUseCase(repo)("  content://exports  ")

        assertEquals("content://exports", repo.currentLocation)
    }

    @Test
    fun `set storage location ignores blank uri`() {
        val repo = FakeStorageRepository().apply { currentLocation = "content://existing" }

        SetStorageLocationUseCase(repo)("   ")

        assertEquals("content://existing", repo.currentLocation)
    }

    @Test
    fun `save document to storage requires pdf uri`() = runTest {
        val storageRepo = FakeStorageRepository()
        val documentRepo = FakeDocumentRepository()
        val document = doc("1", pdfUri = null)

        val result = SaveDocumentToStorageUseCase(storageRepo, documentRepo)(document)

        assertFalse(result)
        assertTrue(storageRepo.savedDocuments.isEmpty())
        assertFalse(documentRepo.observeDocuments().first().any { it.isExportedToStorage })
    }

    @Test
    fun `save document to storage marks document exported on success`() = runTest {
        val storageRepo = FakeStorageRepository(saveResult = true)
        val documentRepo = FakeDocumentRepository()
        val document = doc("1")
        documentRepo.setDocuments(listOf(document))

        val result = SaveDocumentToStorageUseCase(storageRepo, documentRepo)(document)

        assertTrue(result)
        assertEquals(listOf("1"), storageRepo.savedDocuments.map { it.id })
        assertTrue(documentRepo.observeDocuments().first().single().isExportedToStorage)
    }

    @Test
    fun `save document to storage does not mark exported on failure`() = runTest {
        val storageRepo = FakeStorageRepository(saveResult = false)
        val documentRepo = FakeDocumentRepository()
        val document = doc("1")
        documentRepo.setDocuments(listOf(document))

        val result = SaveDocumentToStorageUseCase(storageRepo, documentRepo)(document)

        assertFalse(result)
        assertFalse(documentRepo.observeDocuments().first().single().isExportedToStorage)
    }

    @Test
    fun `create folder trims name and delegates`() = runTest {
        val repo = FakeStorageRepository(createFolderResult = true)

        val result = CreateFolderUseCase(repo)("  Taxes  ")

        assertTrue(result)
    }

    @Test
    fun `create folder rejects blank names`() = runTest {
        val repo = FakeStorageRepository(createFolderResult = true)

        val result = CreateFolderUseCase(repo)("   ")

        assertFalse(result)
    }

    @Test
    fun `batch export skips exported and missing pdf documents and reports mixed results`() = runTest {
        val storageRepo = ResultStorageRepository(
            results = mapOf("1" to true, "2" to false, "3" to true)
        )
        val documentRepo = FakeDocumentRepository()
        documentRepo.setDocuments(
            listOf(
                doc("1"),
                doc("2"),
                doc("3", exported = true),
                doc("4", pdfUri = null),
            )
        )

        val result = BatchExportUseCase(storageRepo, documentRepo)(
            documentRepo.observeDocuments().first()
        )

        assertEquals(1 to 1, result)
        assertEquals(listOf("1", "2"), storageRepo.savedIds)
        val docs = documentRepo.observeDocuments().first().associateBy { it.id }
        assertTrue(docs.getValue("1").isExportedToStorage)
        assertFalse(docs.getValue("2").isExportedToStorage)
        assertTrue(docs.getValue("3").isExportedToStorage)
        assertFalse(docs.getValue("4").isExportedToStorage)
    }

    private class ResultStorageRepository(
        private val results: Map<String, Boolean>,
    ) : StorageRepository {
        val savedIds = mutableListOf<String>()

        override fun observeStorageLocation() = kotlinx.coroutines.flow.flowOf<String?>(null)

        override fun setStorageLocation(uri: String) = Unit

        override suspend fun saveDocument(document: ScannedDocument): Boolean {
            savedIds += document.id
            return results.getValue(document.id)
        }

        override suspend fun createFolder(folderName: String): Boolean = true
    }
}
