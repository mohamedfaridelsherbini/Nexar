import Foundation
import UIKit

enum DocumentStoreError: LocalizedError {
    case emptyScan
    case storageNotConfigured
    case securityScopedAccessDenied
    case previewUnavailable

    var errorDescription: String? {
        switch self {
        case .emptyScan:                  return "No scanned pages were returned."
        case .storageNotConfigured:       return "Choose a storage folder before exporting documents."
        case .securityScopedAccessDenied: return "The selected folder could not be accessed."
        case .previewUnavailable:         return "A preview file could not be prepared for this document."
        }
    }
}

/// Concrete `DocumentRepository` implementation.
///
/// Responsibilities (data layer only — no OCR or classification):
///   - JSON document index in Application Support/Nexar/
///   - Page image files  (`images/{id}/page_*.jpg`)
///   - PDF files         (`pdfs/{id}.pdf`)
///   - Security-scoped bookmark for the user-selected export folder
///   - Export copy to the selected storage folder
actor DocumentStore: DocumentRepository {

    private let fileManager = FileManager.default
    private let defaults = UserDefaults.standard
    private let bookmarkKey = "nexar.storage.bookmark"
    private let indexFileName = "documents.json"

    // MARK: - DocumentRepository: file preparation

    func createDocumentFiles(from images: [UIImage]) async throws -> (stub: ScannedDocument, pageURLs: [URL]) {
        guard !images.isEmpty else { throw DocumentStoreError.emptyScan }
        try ensureDirectories()

        let documentId = UUID().uuidString
        let createdAt = Date()
        let scanDirectory = imagesDirectory.appendingPathComponent(documentId, isDirectory: true)
        try fileManager.createDirectory(at: scanDirectory, withIntermediateDirectories: true)

        var pageFileNames: [String] = []
        var pageURLs: [URL] = []

        for (index, image) in images.enumerated() {
            guard let jpegData = image.jpegData(compressionQuality: 0.82) else { continue }
            let fileName = "page_\(index + 1).jpg"
            let fileURL = scanDirectory.appendingPathComponent(fileName)
            try jpegData.write(to: fileURL, options: .atomic)
            pageFileNames.append("\(documentId)/\(fileName)")
            pageURLs.append(fileURL)
        }

        let pdfFileName = "\(documentId).pdf"
        let pdfURL = pdfsDirectory.appendingPathComponent(pdfFileName)
        try createPDF(from: images, at: pdfURL)

        let stub = ScannedDocument(
            id: documentId,
            name: Self.defaultDocumentName(from: createdAt),
            createdAt: createdAt,
            pageFileNames: pageFileNames,
            pdfFileName: pdfFileName
        )
        return (stub: stub, pageURLs: pageURLs)
    }

    func saveDocument(_ document: ScannedDocument) async throws {
        var docs = try await loadDocuments()
        if let index = docs.firstIndex(where: { $0.id == document.id }) {
            docs[index] = document
        } else {
            docs.insert(document, at: 0)
        }
        try saveDocuments(docs)
    }

    // MARK: - DocumentRepository: CRUD

    func loadDocuments() async throws -> [ScannedDocument] {
        try ensureDirectories()
        let indexURL = baseDirectory.appendingPathComponent(indexFileName)
        guard fileManager.fileExists(atPath: indexURL.path) else { return [] }
        let data = try Data(contentsOf: indexURL)
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode([ScannedDocument].self, from: data)
            .sorted { $0.createdAt > $1.createdAt }
    }

    func renameDocument(id: String, to newName: String) async throws {
        var documents = try await loadDocuments()
        guard let index = documents.firstIndex(where: { $0.id == id }) else { return }
        documents[index].name = newName
        try saveDocuments(documents)
        SpotlightIndexer.shared.index(documents[index])
    }

    func toggleStar(_ document: ScannedDocument) async throws {
        var documents = try await loadDocuments()
        guard let index = documents.firstIndex(where: { $0.id == document.id }) else { return }
        documents[index].isStarred.toggle()
        try saveDocuments(documents)
    }

    func updateDocument(_ document: ScannedDocument) async throws {
        var documents = try await loadDocuments()
        guard let index = documents.firstIndex(where: { $0.id == document.id }) else { return }
        documents[index] = document
        try saveDocuments(documents)
        SpotlightIndexer.shared.index(document)
    }

    func deleteDocument(_ document: ScannedDocument) async throws {
        var documents = try await loadDocuments()
        documents.removeAll { $0.id == document.id }
        try saveDocuments(documents)
        SpotlightIndexer.shared.deindex(document.id)

        let scanDirectory = imagesDirectory.appendingPathComponent(document.id, isDirectory: true)
        if fileManager.fileExists(atPath: scanDirectory.path) {
            try fileManager.removeItem(at: scanDirectory)
        }

        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)
        if fileManager.fileExists(atPath: pdfURL.path) {
            try fileManager.removeItem(at: pdfURL)
        }
    }

    // MARK: - DocumentRepository: export

    func setStorageFolder(_ url: URL) async throws -> String {
        let bookmark = try url.bookmarkData(options: [], includingResourceValuesForKeys: nil, relativeTo: nil)
        defaults.set(bookmark, forKey: bookmarkKey)
        return url.lastPathComponent
    }

    func storageFolderName() async -> String? {
        (try? resolveStorageFolderURL())?.lastPathComponent
    }

    func createFolder(named folderName: String) async throws {
        let folderURL = try storageFolderURL()
        guard folderURL.startAccessingSecurityScopedResource() else {
            throw DocumentStoreError.securityScopedAccessDenied
        }
        defer { folderURL.stopAccessingSecurityScopedResource() }
        let targetURL = folderURL.appendingPathComponent(folderName, isDirectory: true)
        try fileManager.createDirectory(at: targetURL, withIntermediateDirectories: true)
    }

    func exportDocument(_ document: ScannedDocument) async throws {
        let folderURL = try storageFolderURL()
        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)

        guard folderURL.startAccessingSecurityScopedResource() else {
            throw DocumentStoreError.securityScopedAccessDenied
        }
        defer { folderURL.stopAccessingSecurityScopedResource() }

        let year = Calendar.current.component(.year, from: document.createdAt)
        let categoryFolder = folderURL.appendingPathComponent(document.category.folderName, isDirectory: true)
        let yearFolder = categoryFolder.appendingPathComponent(String(year), isDirectory: true)
        try fileManager.createDirectory(at: yearFolder, withIntermediateDirectories: true)

        let destinationURL = yearFolder.appendingPathComponent(exportFileName(for: document))
        if fileManager.fileExists(atPath: destinationURL.path) {
            try fileManager.removeItem(at: destinationURL)
        }
        try fileManager.copyItem(at: pdfURL, to: destinationURL)

        var documents = try await loadDocuments()
        if let index = documents.firstIndex(where: { $0.id == document.id }) {
            documents[index].isExportedToStorage = true
            try saveDocuments(documents)
            let unexportedCount = documents.filter { !$0.isExportedToStorage }.count
            WidgetDataProvider.update(
                unexportedCount: unexportedCount,
                lastScanName: documents[index].name
            )
        }
    }

    // MARK: - DocumentRepository: preview

    func previewURL(for document: ScannedDocument) async -> URL? {
        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)
        if fileManager.fileExists(atPath: pdfURL.path) { return pdfURL }
        guard let firstPage = document.pageFileNames.first else { return nil }
        let imageURL = imagesDirectory.appendingPathComponent(firstPage)
        return fileManager.fileExists(atPath: imageURL.path) ? imageURL : nil
    }

    // MARK: - Private: directories

    private var baseDirectory: URL {
        do {
            let applicationSupport = try fileManager.url(
                for: .applicationSupportDirectory, in: .userDomainMask,
                appropriateFor: nil, create: true
            )
            return applicationSupport.appendingPathComponent("Nexar", isDirectory: true)
        } catch {
            fatalError("Failed to resolve base directory: \(error)")
        }
    }

    private var imagesDirectory: URL { baseDirectory.appendingPathComponent("images", isDirectory: true) }
    private var pdfsDirectory: URL   { baseDirectory.appendingPathComponent("pdfs",   isDirectory: true) }

    private func ensureDirectories() throws {
        try fileManager.createDirectory(at: baseDirectory,   withIntermediateDirectories: true)
        try fileManager.createDirectory(at: imagesDirectory, withIntermediateDirectories: true)
        try fileManager.createDirectory(at: pdfsDirectory,   withIntermediateDirectories: true)
    }

    private func saveDocuments(_ documents: [ScannedDocument]) throws {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        let data = try encoder.encode(documents.sorted { $0.createdAt > $1.createdAt })
        try data.write(to: baseDirectory.appendingPathComponent(indexFileName), options: .atomic)
    }

    private func resolveStorageFolderURL() throws -> URL? {
        guard let bookmarkData = defaults.data(forKey: bookmarkKey) else { return nil }
        var isStale = false
        let url = try URL(resolvingBookmarkData: bookmarkData, options: [], relativeTo: nil, bookmarkDataIsStale: &isStale)
        if isStale {
            let refreshed = try url.bookmarkData(options: [], includingResourceValuesForKeys: nil, relativeTo: nil)
            defaults.set(refreshed, forKey: bookmarkKey)
        }
        return url
    }

    private func storageFolderURL() throws -> URL {
        guard let url = try resolveStorageFolderURL() else { throw DocumentStoreError.storageNotConfigured }
        return url
    }

    // MARK: - Private: PDF generation

    private func createPDF(from images: [UIImage], at url: URL) throws {
        let pageRect = CGRect(x: 0, y: 0, width: 612, height: 792)
        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
        try renderer.writePDF(to: url) { context in
            for image in images {
                context.beginPage()
                let fittedRect = fittedImageRect(for: image.size, in: pageRect.insetBy(dx: 24, dy: 24))
                image.draw(in: fittedRect)
            }
        }
    }

    private func fittedImageRect(for imageSize: CGSize, in bounds: CGRect) -> CGRect {
        let widthScale  = bounds.width  / imageSize.width
        let heightScale = bounds.height / imageSize.height
        let scale = min(widthScale, heightScale)
        let scaledSize = CGSize(width: imageSize.width * scale, height: imageSize.height * scale)
        let origin = CGPoint(x: bounds.midX - scaledSize.width / 2, y: bounds.midY - scaledSize.height / 2)
        return CGRect(origin: origin, size: scaledSize)
    }

    private func exportFileName(for document: ScannedDocument) -> String {
        let trimmed = document.name.trimmingCharacters(in: .whitespacesAndNewlines)
        let safeName = trimmed.isEmpty ? "Document" : trimmed
        return safeName.lowercased().hasSuffix(".pdf") ? safeName : "\(safeName).pdf"
    }

    private static func defaultDocumentName(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        return "Scan \(formatter.string(from: date))"
    }
}
