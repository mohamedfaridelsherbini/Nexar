import Foundation
import SwiftUI
import UIKit

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

@MainActor
final class DashboardViewModel: ObservableObject {
    @Published private(set) var documents: [ScannedDocument] = []
    @Published private(set) var storageFolderName: String?
    @Published var searchText = ""
    @Published var activeFilter: DocumentListFilter = .all
    @Published var sortOrder: DocumentSortOrder = .newest
    @Published var previewItem: PreviewItem?
    @Published var errorMessage: String?
    @Published var batchExportResult: (success: Int, failed: Int)? = nil

    private let store: DocumentStore

    init(store: DocumentStore = DocumentStore()) {
        self.store = store
        Task { await refresh() }
    }

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
            if let cat = activeFilter.category {
                byFilter = documents.filter { $0.category == cat }
            } else {
                byFilter = documents
            }
        }

        let filtered: [ScannedDocument]
        if query.isEmpty {
            filtered = byFilter
        } else {
            filtered = byFilter.filter { doc in
                doc.name.lowercased().contains(query) ||
                doc.ocrText.lowercased().contains(query) ||
                doc.category.rawValue.lowercased().contains(query) ||
                doc.tags.contains { $0.lowercased().contains(query) }
            }
        }

        return filtered.sorted(by: sortOrder)
    }

    func refresh() async {
        do {
            documents = try await store.loadDocuments()
            storageFolderName = await store.storageFolderName()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func handleScannedImages(_ images: [UIImage]) {
        Task {
            do {
                let document = try await store.addDocumentScan(images: images)
                documents.insert(document, at: 0)
                triggerSuccessHaptic()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func renameDocument(_ document: ScannedDocument, to newName: String) {
        let trimmed = newName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        Task {
            do {
                try await store.renameDocument(id: document.id, to: trimmed)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].name = trimmed
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func deleteDocument(_ document: ScannedDocument) {
        Task {
            do {
                try await store.deleteDocument(document)
                documents.removeAll { $0.id == document.id }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func deleteDocuments(at offsets: IndexSet) {
        let items = offsets.map { filteredDocuments[$0] }
        for item in items { deleteDocument(item) }
    }

    func toggleStar(_ document: ScannedDocument) {
        Task {
            do {
                try await store.toggleStar(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].isStarred.toggle()
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func selectStorageFolder(_ url: URL) {
        Task {
            do {
                storageFolderName = try await store.setStorageFolder(url)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func createFolder(named folderName: String) {
        let trimmed = folderName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        Task {
            do { try await store.createFolder(named: trimmed) } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func exportDocument(_ document: ScannedDocument) {
        Task {
            do {
                try await store.exportDocument(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index].isExportedToStorage = true
                }
                triggerSuccessHaptic()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func batchExport() {
        let unexported = documents.filter { !$0.isExportedToStorage }
        guard !unexported.isEmpty else { return }
        Task {
            var success = 0
            var failed = 0
            for doc in unexported {
                do {
                    try await store.exportDocument(doc)
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
                try await store.updateDocument(document)
                if let index = documents.firstIndex(where: { $0.id == document.id }) {
                    documents[index] = document
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func openPreview(for document: ScannedDocument) {
        Task {
            if let url = await store.previewURL(for: document) {
                previewItem = PreviewItem(url: url)
            } else {
                errorMessage = DocumentStoreError.previewUnavailable.localizedDescription
            }
        }
    }
}

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

// MARK: - Haptics
private func triggerSuccessHaptic() {
    let generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(.success)
}
