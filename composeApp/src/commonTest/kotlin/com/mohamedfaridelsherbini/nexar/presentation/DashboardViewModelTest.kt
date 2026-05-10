package com.mohamedfaridelsherbini.nexar.presentation

import com.mohamedfaridelsherbini.nexar.domain.model.DocumentCategory
import com.mohamedfaridelsherbini.nexar.domain.model.ScannedDocument
import com.mohamedfaridelsherbini.nexar.domain.usecase.AddScannedDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.BatchExportUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.CreateFolderUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import com.mohamedfaridelsherbini.nexar.domain.usecase.DeleteDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.MarkExportedUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveDocumentsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ObserveStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ProcessScannedDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.RenameDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SaveDocumentToStorageUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SearchDocumentsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.SetStorageLocationUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.StorageAnalyticsUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.ToggleStarUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.UpdateDocumentUseCase
import com.mohamedfaridelsherbini.nexar.fakes.FakeClassifierService
import com.mohamedfaridelsherbini.nexar.fakes.FakeDocumentRepository
import com.mohamedfaridelsherbini.nexar.fakes.FakeDuplicateDetectionService
import com.mohamedfaridelsherbini.nexar.fakes.FakeExtractionService
import com.mohamedfaridelsherbini.nexar.fakes.FakeNamingService
import com.mohamedfaridelsherbini.nexar.fakes.FakeOcrProcessor
import com.mohamedfaridelsherbini.nexar.fakes.FakeStorageRepository
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardFilter
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardViewModel
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.NexarError
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.SortOrder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    /**
     * Shared scheduler so that both [testDispatcher] (used by [viewModelScope] via setMain)
     * and [runTest]'s internal scope advance together via [advanceUntilIdle].
     */
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var docRepo: FakeDocumentRepository
    private lateinit var storageRepo: FakeStorageRepository
    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        docRepo = FakeDocumentRepository()
        storageRepo = FakeStorageRepository()
        viewModel = buildViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(ocrThrows: Boolean = false): DashboardViewModel {
        val ocr: com.mohamedfaridelsherbini.nexar.domain.usecase.OcrProcessor =
            if (ocrThrows) ThrowingOcrProcessor() else FakeOcrProcessor("")
        val processUseCase =
            ProcessScannedDocumentUseCase(
                ocrProcessor = ocr,
                classifier = FakeClassifierService(DocumentCategory.Invoice),
                namer = FakeNamingService("Invoice - 2024"),
                extractor = FakeExtractionService("\$10.00", "2024-01-01"),
                duplicateDetector = FakeDuplicateDetectionService(null),
                documentRepository = docRepo,
            )
        val useCases =
            DashboardUseCases(
                observeDocuments = ObserveDocumentsUseCase(docRepo),
                addScannedDocument = AddScannedDocumentUseCase(docRepo),
                updateDocument = UpdateDocumentUseCase(docRepo),
                renameDocument = RenameDocumentUseCase(docRepo),
                deleteDocument = DeleteDocumentUseCase(docRepo),
                markExported = MarkExportedUseCase(docRepo),
                toggleStar = ToggleStarUseCase(docRepo),
                observeStorageLocation = ObserveStorageLocationUseCase(storageRepo),
                setStorageLocation = SetStorageLocationUseCase(storageRepo),
                saveDocumentToStorage = SaveDocumentToStorageUseCase(storageRepo, docRepo),
                batchExport = BatchExportUseCase(storageRepo, docRepo),
                createFolder = CreateFolderUseCase(storageRepo),
            )
        return DashboardViewModel(
            useCases = useCases,
            processScannedDocument = processUseCase,
            analyticsUseCase = StorageAnalyticsUseCase(),
            searchDocuments = SearchDocumentsUseCase(docRepo),
        )
    }

    /**
     * Subscribes to [vm]'s [DashboardViewModel.uiState] so [SharingStarted.WhileSubscribed]
     * activates the upstream [combine]. Must be called inside [runTest].
     */
    private fun kotlinx.coroutines.CoroutineScope.activateState(vm: DashboardViewModel = viewModel): Job = launch { vm.uiState.collect { } }

    private fun doc(
        id: String = "1",
        name: String = "Doc",
        category: DocumentCategory = DocumentCategory.Other,
        isExported: Boolean = false,
        isStarred: Boolean = false,
    ) = ScannedDocument(
        id = id,
        name = name,
        dateMillis = 0L,
        imageUris = listOf("img.jpg"),
        category = category,
        isExportedToStorage = isExported,
        isStarred = isStarred,
    )

    // ── Loading state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoadingDocuments true before any subscription`() =
        runTest(testDispatcher) {
            // Read initialValue before any upstream runs — should still be the default
            assertTrue(viewModel.uiState.value.isLoadingDocuments)
        }

    @Test
    fun `isLoadingDocuments becomes false after repository emits`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()
            // After upstream runs, the initial empty-list emission flips hasLoaded → true
            assertFalse(viewModel.uiState.value.isLoadingDocuments)
            job.cancel()
        }

    // ── Documents & visible list ──────────────────────────────────────────────

    @Test
    fun `uiState reflects documents from repository`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1"), doc("2")))
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.documents.size)
            job.cancel()
        }

    @Test
    fun `visibleDocuments matches all documents with no filter or search`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1"), doc("2"), doc("3")))
            advanceUntilIdle()

            assertEquals(3, viewModel.uiState.value.visibleDocuments.size)
            job.cancel()
        }

    // ── Search ────────────────────────────────────────────────────────────────

    @Test
    fun `search filters visibleDocuments by name`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", "Invoice Jan"), doc("2", "Receipt Feb")))
            advanceUntilIdle()

            viewModel.onSearchQueryChanged("invoice")
            advanceUntilIdle()

            val visible = viewModel.uiState.value.visibleDocuments
            assertEquals(1, visible.size)
            assertEquals("Invoice Jan", visible.first().name)
            job.cancel()
        }

    @Test
    fun `clearing search restores all documents`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", "Invoice"), doc("2", "Receipt")))
            advanceUntilIdle()

            viewModel.onSearchQueryChanged("invoice")
            advanceUntilIdle()
            viewModel.onSearchQueryChanged("")
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.visibleDocuments.size)
            job.cancel()
        }

    // ── Filter ────────────────────────────────────────────────────────────────

    @Test
    fun `Starred filter shows only starred documents`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", isStarred = true), doc("2", isStarred = false)))
            advanceUntilIdle()

            viewModel.onFilterChanged(DashboardFilter.Starred)
            advanceUntilIdle()

            val visible = viewModel.uiState.value.visibleDocuments
            assertEquals(1, visible.size)
            assertTrue(visible.first().isStarred)
            job.cancel()
        }

    @Test
    fun `NeedsExport filter shows only unexported documents`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", isExported = false), doc("2", isExported = true)))
            advanceUntilIdle()

            viewModel.onFilterChanged(DashboardFilter.NeedsExport)
            advanceUntilIdle()

            val visible = viewModel.uiState.value.visibleDocuments
            assertEquals(1, visible.size)
            assertFalse(visible.first().isExportedToStorage)
            job.cancel()
        }

    @Test
    fun `Invoice category filter shows only invoice documents`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(
                listOf(
                    doc("1", category = DocumentCategory.Invoice),
                    doc("2", category = DocumentCategory.Receipt),
                ),
            )
            advanceUntilIdle()

            viewModel.onFilterChanged(DashboardFilter.Invoice)
            advanceUntilIdle()

            val visible = viewModel.uiState.value.visibleDocuments
            assertEquals(1, visible.size)
            assertEquals(DocumentCategory.Invoice, visible.first().category)
            job.cancel()
        }

    // ── Sort ──────────────────────────────────────────────────────────────────

    @Test
    fun `sort Newest orders documents descending by date`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(
                listOf(
                    doc("1").copy(dateMillis = 1000L),
                    doc("2").copy(dateMillis = 2000L),
                ),
            )
            advanceUntilIdle()

            viewModel.onSortChanged(SortOrder.Newest)
            advanceUntilIdle()

            val docs = viewModel.uiState.value.visibleDocuments
            assertEquals("2", docs.first().id)
            assertEquals("1", docs.last().id)
            job.cancel()
        }

    @Test
    fun `sort Oldest orders documents ascending by date`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(
                listOf(
                    doc("1").copy(dateMillis = 2000L),
                    doc("2").copy(dateMillis = 1000L),
                ),
            )
            advanceUntilIdle()

            viewModel.onSortChanged(SortOrder.Oldest)
            advanceUntilIdle()

            assertEquals("2", viewModel.uiState.value.visibleDocuments.first().id)
            job.cancel()
        }

    @Test
    fun `sort NameAsc orders documents alphabetically`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", "Zebra Doc"), doc("2", "Alpha Doc")))
            advanceUntilIdle()

            viewModel.onSortChanged(SortOrder.NameAsc)
            advanceUntilIdle()

            assertEquals("Alpha Doc", viewModel.uiState.value.visibleDocuments.first().name)
            job.cancel()
        }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `OCR failure sets OcrFailed error`() =
        runTest(testDispatcher) {
            val vm = buildViewModel(ocrThrows = true)
            val job = activateState(vm)
            advanceUntilIdle()

            vm.onDocumentScanned(doc("fail-doc", "Failing Scan"))
            advanceUntilIdle()

            assertIs<NexarError.OcrFailed>(vm.uiState.value.error)
            job.cancel()
        }

    @Test
    fun `onErrorDismissed clears the error`() =
        runTest(testDispatcher) {
            val vm = buildViewModel(ocrThrows = true)
            val job = activateState(vm)
            advanceUntilIdle()

            vm.onDocumentScanned(doc("x", "Scan"))
            advanceUntilIdle()
            assertIs<NexarError.OcrFailed>(vm.uiState.value.error)

            vm.onErrorDismissed()
            advanceUntilIdle()
            assertNull(vm.uiState.value.error)
            job.cancel()
        }

    @Test
    fun `export failure sets ExportFailed error`() =
        runTest(testDispatcher) {
            storageRepo = FakeStorageRepository(saveResult = false)
            val vm = buildViewModel() // wires the new failing storageRepo
            val job = activateState(vm)
            advanceUntilIdle()

            // pdfUri must be non-null so SaveDocumentToStorageUseCase proceeds past the early return
            val exportableDoc = doc("1", "My Doc").copy(pdfUri = "file://my-doc.pdf")
            vm.onSaveDocumentToStorage(exportableDoc)
            advanceUntilIdle()

            assertIs<NexarError.ExportFailed>(vm.uiState.value.error)
            job.cancel()
        }

    // ── Analytics ─────────────────────────────────────────────────────────────

    @Test
    fun `analytics totalDocuments equals document count`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1"), doc("2"), doc("3")))
            advanceUntilIdle()

            assertEquals(3, viewModel.uiState.value.analytics.totalDocuments)
            job.cancel()
        }

    @Test
    fun `analytics exportedDocuments reflects exported count`() =
        runTest(testDispatcher) {
            val job = activateState()
            advanceUntilIdle()

            docRepo.setDocuments(listOf(doc("1", isExported = true), doc("2"), doc("3", isExported = true)))
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.analytics.exportedDocuments)
            job.cancel()
        }
}

/** An [OcrProcessor] that always throws to simulate engine failures. */
private class ThrowingOcrProcessor : com.mohamedfaridelsherbini.nexar.domain.usecase.OcrProcessor {
    override suspend fun extractText(imageUris: List<String>): String = throw RuntimeException("Simulated OCR engine failure")
}
