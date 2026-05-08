import Foundation
import UIKit
import Vision

/// Orchestrates the full post-scan intelligence pipeline.
/// Mirrors KMP's `ProcessScannedDocumentUseCase` in `commonMain`.
///
/// Responsibilities (single pass after the camera returns images):
///   1. Delegate file I/O (images + PDF) to the `DocumentRepository`
///   2. Run Vision OCR on the saved page images
///   3. Classify → name → extract amounts/dates → detect duplicates
///   4. Persist the enriched document via the repository
///   5. Update Spotlight index and widget shared data
final class ProcessDocumentUseCase {

    private let repository: DocumentRepository

    init(repository: DocumentRepository) {
        self.repository = repository
    }

    // MARK: - Public API

    /// Full pipeline entry point.  Call from `DashboardViewModel.handleScannedImages(_:)`.
    func execute(images: [UIImage]) async throws -> ScannedDocument {
        // 1. Save images + PDF, get stub document and page URLs for OCR
        let (stub, pageURLs) = try await repository.createDocumentFiles(from: images)

        // 2. Vision OCR (async, Vision callbacks bridged via withCheckedContinuation)
        let ocrText = await extractOcrText(from: pageURLs)

        // 3. Classify + name + extract intelligence
        var document = stub
        let (category, tags) = DocumentClassifier.classify(ocrText)
        document.name = DocumentNamer.suggest(
            ocrText: ocrText,
            category: category,
            date: stub.createdAt
        ) ?? stub.name
        document.ocrText = ocrText
        document.category = category
        document.tags = tags
        document.ocrProcessed = true
        document.extractedAmount = DocumentExtractor.extractAmount(from: ocrText)
        document.extractedDate = DocumentExtractor.extractDate(from: ocrText)

        // 4. Duplicate detection against the existing index
        let existing = try await repository.loadDocuments()
        document.duplicateOfId = DuplicateDetector.findDuplicate(newDoc: document, in: existing)

        // 5. Persist enriched document
        try await repository.saveDocument(document)

        // 6. Side effects: Spotlight + widget
        SpotlightIndexer.shared.index(document)
        let unexportedCount = (existing + [document]).filter { !$0.isExportedToStorage }.count
        WidgetDataProvider.update(unexportedCount: unexportedCount, lastScanName: document.name)

        return document
    }

    // MARK: - Vision OCR (private)

    private func extractOcrText(from urls: [URL]) async -> String {
        var parts: [String] = []
        for url in urls {
            if let text = await recognizeText(in: url), !text.isEmpty {
                parts.append(text)
            }
        }
        return parts.joined(separator: "\n")
    }

    private func recognizeText(in url: URL) async -> String? {
        await withCheckedContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                guard error == nil,
                      let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(returning: nil)
                    return
                }
                let lines = observations.compactMap { $0.topCandidates(1).first?.string }
                continuation.resume(returning: lines.joined(separator: " "))
            }
            request.recognitionLevel = .accurate

            let handler = VNImageRequestHandler(url: url, options: [:])
            do {
                try handler.perform([request])
            } catch {
                continuation.resume(returning: nil)
            }
        }
    }
}
