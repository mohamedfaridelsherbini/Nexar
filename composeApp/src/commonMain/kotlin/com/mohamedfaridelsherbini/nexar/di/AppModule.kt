package com.mohamedfaridelsherbini.nexar.di

import com.mohamedfaridelsherbini.nexar.domain.classifier.ClassifierService
import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentClassifier
import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentExtractor
import com.mohamedfaridelsherbini.nexar.domain.classifier.DocumentNamer
import com.mohamedfaridelsherbini.nexar.domain.classifier.DuplicateDetectionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.DuplicateDetector
import com.mohamedfaridelsherbini.nexar.domain.classifier.ExtractionService
import com.mohamedfaridelsherbini.nexar.domain.classifier.NamingService
import com.mohamedfaridelsherbini.nexar.domain.usecase.AddScannedDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.BatchExportUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.CreateFolderUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.DashboardUseCases
import com.mohamedfaridelsherbini.nexar.domain.usecase.DeleteDocumentUseCase
import com.mohamedfaridelsherbini.nexar.domain.usecase.GetSettingsUseCase
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
import com.mohamedfaridelsherbini.nexar.domain.usecase.createOcrProcessor
import com.mohamedfaridelsherbini.nexar.platform.createPermissionHealthProvider
import com.mohamedfaridelsherbini.nexar.presentation.dashboard.DashboardViewModel
import com.mohamedfaridelsherbini.nexar.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val commonModule =
    module {

        // ── OCR ───────────────────────────────────────────────────────────────────
        single { createOcrProcessor() }

        // ── Classifier services (bound as interfaces for testability) ─────────────
        single<ClassifierService> { DocumentClassifier }
        single<NamingService> { DocumentNamer }
        single<ExtractionService> { DocumentExtractor }
        single<DuplicateDetectionService> { DuplicateDetector }

        // ── Analytics ─────────────────────────────────────────────────────────────
        single { StorageAnalyticsUseCase() }

        // ── Document use cases ────────────────────────────────────────────────────
        single { ObserveDocumentsUseCase(get()) }
        single { AddScannedDocumentUseCase(get()) }
        single { UpdateDocumentUseCase(get()) }
        single { RenameDocumentUseCase(get()) }
        single { DeleteDocumentUseCase(get()) }
        single { MarkExportedUseCase(get()) }
        single { ToggleStarUseCase(get()) }
        single { SearchDocumentsUseCase(get()) }

        // ── Storage use cases ─────────────────────────────────────────────────────
        single { ObserveStorageLocationUseCase(get()) }
        single { SetStorageLocationUseCase(get()) }
        single { SaveDocumentToStorageUseCase(get(), get()) }
        single { BatchExportUseCase(get(), get()) }
        single { CreateFolderUseCase(get()) }

        // ── Settings ──────────────────────────────────────────────────────────────
        single { GetSettingsUseCase() }
        single { createPermissionHealthProvider() }

        // ── Post-scan intelligence pipeline ───────────────────────────────────────
        single {
            ProcessScannedDocumentUseCase(
                ocrProcessor = get(),
                classifier = get(),
                namer = get(),
                extractor = get(),
                duplicateDetector = get(),
                documentRepository = get(),
            )
        }

        // ── Dashboard use cases bundle ────────────────────────────────────────────
        single {
            DashboardUseCases(
                observeDocuments = get(),
                addScannedDocument = get(),
                updateDocument = get(),
                renameDocument = get(),
                deleteDocument = get(),
                markExported = get(),
                toggleStar = get(),
                observeStorageLocation = get(),
                setStorageLocation = get(),
                saveDocumentToStorage = get(),
                batchExport = get(),
                createFolder = get(),
            )
        }

        // ── ViewModel ─────────────────────────────────────────────────────────────
        viewModel {
            DashboardViewModel(
                useCases = get(),
                processScannedDocument = get(),
                analyticsUseCase = get(),
                searchDocuments = get(),
            )
        }

        viewModel {
            SettingsViewModel(
                getSettings = get(),
                observeStorageLocation = get(),
                permissionHealthProvider = get(),
            )
        }
    }
