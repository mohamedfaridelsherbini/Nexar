import Foundation
import SwiftUI
import UIKit

// MARK: - Supporting types

struct PreviewItem: Identifiable {
    let id = UUID()
    let url: URL
}

enum DocumentListFilter: String, CaseIterable {
    case all
    case needsExport
    case starred
    case receipt
    case invoice
    case idDocument
    case contract
    case medical

    var label: String {
        switch self {
        case .all:         return "All"
        case .needsExport: return "Needs export"
        case .starred:     return "⭐ Starred"
        case .receipt:     return "Receipts"
        case .invoice:     return "Invoices"
        case .idDocument:  return "IDs"
        case .contract:    return "Contracts"
        case .medical:     return "Medical"
        }
    }

    var category: DocumentCategory? {
        switch self {
        case .receipt:     return .receipt
        case .invoice:     return .invoice
        case .idDocument:  return .idDocument
        case .contract:    return .contract
        case .medical:     return .medical
        default:           return nil
        }
    }
}

enum DocumentSortOrder: String, CaseIterable {
    case newest   = "Newest first"
    case oldest   = "Oldest first"
    case nameAsc  = "Name A→Z"
    case category = "Category"
}

// MARK: - DashboardViewModel

@MainActor
final class DashboardViewModel: ObservableObject {

    // MARK: Document list

    @Published private(set) var documents: [ScannedDocument] = []
    @Published private(set) var storageFolderName: String?

    // MARK: UI preferences

    @Published var searchText = ""
    @Published var activeFilter: DocumentListFilter = .all
    @Published var sortOrder: DocumentSortOrder = .newest
    @Published var previewItem: PreviewItem?

    // MARK: Loading states (mirrors KMP DashboardUiState)

    /// True while a new scan is being processed (OCR + classification in flight).
    @Published private(set) var isProcessing = false
    /// Set to the document ID while a single-document export is running.
    @Published private(set) var exportingDocumentId: String? = nil
    /// True while a batch export is in progress.
    @Published private(set) var isBatchExporting = false

    // MARK: Error state (typed, mirrors NexarError.kt)

    @Published var error: NexarError? = nil

    // MARK: Batch export result

    @Published var batchExportResult: (success: Int, failed: Int)? = nil

    // MARK: Dependencies

    private let repository: DocumentRepository
    private let processDocument: ProcessDocumentUseCase

    // MARK: Init

    /// Convenience initialiser: creates a `DocumentStore` + `ProcessDocumentUseCase` automatically.
    convenience init() {
        let store = DocumentStore()
        self.init(repository: store)
    }

    init(repository: DocumentRepository, processDocument: ProcessDocumentUseCase? = nil) {
        self.repository = repository
        self.processDocument = processDocument ?? ProcessDocumentUseCase(repository: repository)
        Task { await refresh() }
    }

    // MARK: - Computed

    var needsExportCount: Int {
        documents.filter { !$0.isExportedToStorage }.count
    }

    var filteredDocuments: [ScannedDocument] {
        let query = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()

        let byFilter: [ScannedDocument]
        switch activeFilter {
        case .all:
            byFilter = documents
        case .needsExport:
            byFilter = documents.filter { !$0.isExportedToStorage }
        case .starred:
            byFilter = documents.filter { $0.isStarred }
        default:
            byFilter = activeFilter.category.map { cat in documents.filter { $0.category == cat } }
                ?? documents
        }

        guard !query.isEmpty else { return byFilter.sorted(by: sortOrder) }

        return byFilter.filter { doc in
            doc.name.lowercased().contains(query) ||
            doc.ocrText.lowercased().contains(query) ||
            doc.category.rawValue.lowercased().contains(query) ||
            doc.tags.contains { $0.lowercased().contains(query) }
        }.sorted(by: sortOrder)
    }

    // MARK: - Actions

    func refresh() async {
        do {
            documents = try await repository.loadDocuments()
            storageFolderName = await repository.storageFolderName()
        } catch {
            self.error = .ocrFailed("loading documents")
        }
    }

    func handleScannedImages(_ images: [UIImage]) {
        isProcessing = true
        Task {
            defer { isProcessing = false }
            do {
                let document = try await processDocument.execute(images: images)
                documents.insert(document, at: 0)
                triggerSuccessHaptic()
            } catch {
                self.error = .ocrFailed("new scan")
            }
        }
    }

    func renameDocument(_ document: ScannedDocument, to newName: String) {
        let trimmed = newName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        Task {
            do {
                try await repository.renameDocument(id: document.id, to: trimmed)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].name = trimmed
                }
            } catch {
                self.error = .ocrFailed(document.name)
            }
        }
    }

    func deleteDocument(_ document: ScannedDocument) {
        Task {
            do {
                try await repository.deleteDocument(document)
                documents.removeAll { $0.id == document.id }
            } catch {
                self.error = .ocrFailed(document.name)
            }
        }
    }

    func toggleStar(_ document: ScannedDocument) {
        Task {
            do {
                try await repository.toggleStar(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].isStarred.toggle()
                }
            } catch {
                self.error = .ocrFailed(document.name)
            }
        }
    }

    func selectStorageFolder(_ url: URL) {
        Task {
            do {
                storageFolderName = try await repository.setStorageFolder(url)
            } catch {
                self.error = .folderCreationFailed
            }
        }
    }

    func createFolder(named folderName: String) {
        let trimmed = folderName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        Task {
            do {
                try await repository.createFolder(named: trimmed)
            } catch {
                self.error = .folderCreationFailed
            }
        }
    }

    func exportDocument(_ document: ScannedDocument) {
        exportingDocumentId = document.id
        Task {
            defer { exportingDocumentId = nil }
            do {
                try await repository.exportDocument(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].isExportedToStorage = true
                }
                triggerSuccessHaptic()
            } catch {
                self.error = .exportFailed(document.name)
            }
        }
    }

    func batchExport() {
        let unexported = documents.filter { !$0.isExportedToStorage }
        guard !unexported.isEmpty else { return }
        isBatchExporting = true
        Task {
            defer { isBatchExporting = false }
            var success = 0
            var failed = 0
            for doc in unexported {
                do {
                    try await repository.exportDocument(doc)
                    if let index = documents.firstIndex(where: { $0.id == doc.id }) {
                        documents[index].isExportedToStorage = true
                    }
                    success += 1
                } catch {
                    failed += 1
                }
            }
            batchExportResult = (success: success, failed: failed)
            if success > 0 { triggerSuccessHaptic() }
        }
    }

    func updateDocument(_ document: ScannedDocument) {
        Task {
            do {
                try await repository.updateDocument(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index] = document
                }
            } catch {
                self.error = .ocrFailed(document.name)
            }
        }
    }

    func openPreview(for document: ScannedDocument) {
        Task {
            if let url = await repository.previewURL(for: document) {
                previewItem = PreviewItem(url: url)
            } else {
                self.error = .previewUnavailable
            }
        }
    }

    func dismissError() {
        error = nil
    }
}

// MARK: - Helpers

private extension Array where Element == ScannedDocument {
    func sorted(by order: DocumentSortOrder) -> [ScannedDocument] {
        switch order {
        case .newest:   return sorted { $0.createdAt > $1.createdAt }
        case .oldest:   return sorted { $0.createdAt < $1.createdAt }
        case .nameAsc:  return sorted { $0.name.lowercased() < $1.name.lowercased() }
        case .category: return sorted { $0.category.rawValue < $1.category.rawValue }
        }
    }
}

private func triggerSuccessHaptic() {
    let generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(.success)
}
