import Foundation
import SwiftUI
import UIKit

struct PreviewItem: Identifiable {
    let id = UUID()
    let url: URL
}

@MainActor
final class DashboardViewModel: ObservableObject {
    @Published private(set) var documents: [ScannedDocument] = []
    @Published private(set) var storageFolderName: String?
    @Published var searchText = ""
    @Published var previewItem: PreviewItem?
    @Published var errorMessage: String?

    private let store: DocumentStore

    init(store: DocumentStore = DocumentStore()) {
        self.store = store

        Task {
            await refresh()
        }
    }

    var filteredDocuments: [ScannedDocument] {
        let query = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return documents }

        return documents.filter {
            $0.name.localizedCaseInsensitiveContains(query)
        }
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

    func deleteDocuments(at offsets: IndexSet) {
        let items = offsets.map { filteredDocuments[$0] }

        Task {
            do {
                for item in items {
                    try await store.deleteDocument(item)
                }
                documents.removeAll { doc in items.contains(doc) }
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
            do {
                try await store.createFolder(named: trimmed)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func exportDocument(_ document: ScannedDocument) {
        Task {
            do {
                try await store.exportDocument(document)
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
