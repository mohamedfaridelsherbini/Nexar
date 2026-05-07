import Foundation
import UIKit

enum DocumentStoreError: LocalizedError {
    case emptyScan
    case storageNotConfigured
    case securityScopedAccessDenied
    case previewUnavailable

    var errorDescription: String? {
        switch self {
        case .emptyScan:
            return "No scanned pages were returned."
        case .storageNotConfigured:
            return "Choose a storage folder before exporting documents."
        case .securityScopedAccessDenied:
            return "The selected folder could not be accessed."
        case .previewUnavailable:
            return "A preview file could not be prepared for this document."
        }
    }
}

actor DocumentStore {
    private let fileManager = FileManager.default
    private let defaults = UserDefaults.standard
    private let bookmarkKey = "nexar.storage.bookmark"
    private let indexFileName = "documents.json"

    func loadDocuments() throws -> [ScannedDocument] {
        try ensureDirectories()
        let indexURL = baseDirectory.appendingPathComponent(indexFileName)

        guard fileManager.fileExists(atPath: indexURL.path) else {
            return []
        }

        let data = try Data(contentsOf: indexURL)
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode([ScannedDocument].self, from: data)
            .sorted { $0.createdAt > $1.createdAt }
    }

    func addDocumentScan(images: [UIImage]) throws -> ScannedDocument {
        guard !images.isEmpty else {
            throw DocumentStoreError.emptyScan
        }

        try ensureDirectories()

        let documentId = UUID().uuidString
        let createdAt = Date()
        let scanDirectory = imagesDirectory.appendingPathComponent(documentId, isDirectory: true)
        try fileManager.createDirectory(at: scanDirectory, withIntermediateDirectories: true)

        var pageFileNames: [String] = []

        for (index, image) in images.enumerated() {
            guard let jpegData = image.jpegData(compressionQuality: 0.82) else { continue }
            let fileName = "page_\(index + 1).jpg"
            let fileURL = scanDirectory.appendingPathComponent(fileName)
            try jpegData.write(to: fileURL, options: .atomic)
            pageFileNames.append("\(documentId)/\(fileName)")
        }

        let pdfFileName = "\(documentId).pdf"
        let pdfURL = pdfsDirectory.appendingPathComponent(pdfFileName)
        try createPDF(from: images, at: pdfURL)

        let document = ScannedDocument(
            id: documentId,
            name: Self.defaultDocumentName(from: createdAt),
            createdAt: createdAt,
            pageFileNames: pageFileNames,
            pdfFileName: pdfFileName
        )

        var documents = try loadDocuments()
        documents.insert(document, at: 0)
        try saveDocuments(documents)

        return document
    }

    func renameDocument(id: String, to newName: String) throws {
        var documents = try loadDocuments()
        guard let index = documents.firstIndex(where: { $0.id == id }) else { return }
        documents[index].name = newName
        try saveDocuments(documents)
    }

    func deleteDocument(_ document: ScannedDocument) throws {
        var documents = try loadDocuments()
        documents.removeAll { $0.id == document.id }
        try saveDocuments(documents)

        let scanDirectory = imagesDirectory.appendingPathComponent(document.id, isDirectory: true)
        if fileManager.fileExists(atPath: scanDirectory.path) {
            try fileManager.removeItem(at: scanDirectory)
        }

        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)
        if fileManager.fileExists(atPath: pdfURL.path) {
            try fileManager.removeItem(at: pdfURL)
        }
    }

    func setStorageFolder(_ url: URL) throws -> String {
        let bookmark = try url.bookmarkData(
            options: [],
            includingResourceValuesForKeys: nil,
            relativeTo: nil
        )
        defaults.set(bookmark, forKey: bookmarkKey)
        return url.lastPathComponent
    }

    func storageFolderName() -> String? {
        (try? resolveStorageFolderURL())?.lastPathComponent
    }

    func createFolder(named folderName: String) throws {
        let folderURL = try storageFolderURL()
        guard folderURL.startAccessingSecurityScopedResource() else {
            throw DocumentStoreError.securityScopedAccessDenied
        }
        defer { folderURL.stopAccessingSecurityScopedResource() }

        let targetURL = folderURL.appendingPathComponent(folderName, isDirectory: true)
        try fileManager.createDirectory(at: targetURL, withIntermediateDirectories: true)
    }

    func exportDocument(_ document: ScannedDocument) throws {
        let folderURL = try storageFolderURL()
        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)
        let destinationURL = folderURL.appendingPathComponent(exportFileName(for: document))

        guard folderURL.startAccessingSecurityScopedResource() else {
            throw DocumentStoreError.securityScopedAccessDenied
        }
        defer { folderURL.stopAccessingSecurityScopedResource() }

        if fileManager.fileExists(atPath: destinationURL.path) {
            try fileManager.removeItem(at: destinationURL)
        }

        try fileManager.copyItem(at: pdfURL, to: destinationURL)
    }

    func previewURL(for document: ScannedDocument) -> URL? {
        let pdfURL = pdfsDirectory.appendingPathComponent(document.pdfFileName)
        if fileManager.fileExists(atPath: pdfURL.path) {
            return pdfURL
        }

        guard let firstPage = document.pageFileNames.first else { return nil }
        let imageURL = imagesDirectory.appendingPathComponent(firstPage)
        return fileManager.fileExists(atPath: imageURL.path) ? imageURL : nil
    }

    private var baseDirectory: URL {
        let applicationSupport = try! fileManager.url(
            for: .applicationSupportDirectory,
            in: .userDomainMask,
            appropriateFor: nil,
            create: true
        )
        return applicationSupport.appendingPathComponent("Nexar", isDirectory: true)
    }

    private var imagesDirectory: URL {
        baseDirectory.appendingPathComponent("images", isDirectory: true)
    }

    private var pdfsDirectory: URL {
        baseDirectory.appendingPathComponent("pdfs", isDirectory: true)
    }

    private func ensureDirectories() throws {
        try fileManager.createDirectory(at: baseDirectory, withIntermediateDirectories: true)
        try fileManager.createDirectory(at: imagesDirectory, withIntermediateDirectories: true)
        try fileManager.createDirectory(at: pdfsDirectory, withIntermediateDirectories: true)
    }

    private func saveDocuments(_ documents: [ScannedDocument]) throws {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]

        let data = try encoder.encode(documents.sorted { $0.createdAt > $1.createdAt })
        try data.write(to: baseDirectory.appendingPathComponent(indexFileName), options: .atomic)
    }

    private func resolveStorageFolderURL() throws -> URL? {
        guard let bookmarkData = defaults.data(forKey: bookmarkKey) else {
            return nil
        }

        var isStale = false
        let url = try URL(
            resolvingBookmarkData: bookmarkData,
            options: [],
            relativeTo: nil,
            bookmarkDataIsStale: &isStale
        )

        if isStale {
            let refreshedBookmark = try url.bookmarkData(
                options: [],
                includingResourceValuesForKeys: nil,
                relativeTo: nil
            )
            defaults.set(refreshedBookmark, forKey: bookmarkKey)
        }

        return url
    }

    private func storageFolderURL() throws -> URL {
        guard let url = try resolveStorageFolderURL() else {
            throw DocumentStoreError.storageNotConfigured
        }
        return url
    }

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
        let widthScale = bounds.width / imageSize.width
        let heightScale = bounds.height / imageSize.height
        let scale = min(widthScale, heightScale)

        let scaledSize = CGSize(
            width: imageSize.width * scale,
            height: imageSize.height * scale
        )

        let origin = CGPoint(
            x: bounds.midX - scaledSize.width / 2,
            y: bounds.midY - scaledSize.height / 2
        )

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
