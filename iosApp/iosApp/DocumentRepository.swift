import UIKit

/// Data-layer contract for document persistence.
/// Mirrors the KMP `DocumentRepository` interface in `commonMain`.
///
/// The concrete implementation is `DocumentStore`.
/// Any conforming type can be used in `DashboardViewModel` and `ProcessDocumentUseCase`,
/// enabling full testability via mock implementations.
///
/// All methods are declared `async` so that actor-based implementations (like `DocumentStore`)
/// can conform without data-race diagnostics — callers always use `await`.
protocol DocumentRepository: AnyObject {

    // MARK: - File preparation (called by ProcessDocumentUseCase)

    /// Saves page images + generates a PDF, then returns a stub document (no OCR yet)
    /// and the on-disk page URLs so the use case can run Vision OCR on them.
    func createDocumentFiles(from images: [UIImage]) async throws -> (stub: ScannedDocument, pageURLs: [URL])

    /// Persists a fully-processed document to the JSON index.
    func saveDocument(_ document: ScannedDocument) async throws

    // MARK: - CRUD

    func loadDocuments() async throws -> [ScannedDocument]
    func updateDocument(_ document: ScannedDocument) async throws
    func renameDocument(id: String, to newName: String) async throws
    func toggleStar(_ document: ScannedDocument) async throws
    func deleteDocument(_ document: ScannedDocument) async throws

    // MARK: - Export

    func exportDocument(_ document: ScannedDocument) async throws
    func setStorageFolder(_ url: URL) async throws -> String
    func storageFolderName() async -> String?
    func createFolder(named name: String) async throws

    // MARK: - Preview

    func previewURL(for document: ScannedDocument) async -> URL?
}
