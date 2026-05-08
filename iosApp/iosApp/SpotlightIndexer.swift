import CoreSpotlight
import MobileCoreServices
import UniformTypeIdentifiers

final class SpotlightIndexer {
    static let shared = SpotlightIndexer()
    private init() {}

    private let domainIdentifier = "com.mohamedfaridelsherbini.nexar.documents"

    func index(_ document: ScannedDocument) {
        let attributeSet = CSSearchableItemAttributeSet(contentType: UTType.pdf)
        attributeSet.title = document.name
        attributeSet.contentDescription = document.ocrText.isEmpty
            ? "Scanned document — \(document.category.rawValue)"
            : String(document.ocrText.prefix(300))
        attributeSet.keywords = document.tags + [document.category.rawValue]

        let item = CSSearchableItem(
            uniqueIdentifier: document.id,
            domainIdentifier: domainIdentifier,
            attributeSet: attributeSet
        )
        item.expirationDate = .distantFuture

        CSSearchableIndex.default().indexSearchableItems([item]) { error in
            if let error { print("[Spotlight] index error: \(error.localizedDescription)") }
        }
    }

    func deindex(_ documentId: String) {
        CSSearchableIndex.default().deleteSearchableItems(
            withIdentifiers: [documentId]
        ) { error in
            if let error { print("[Spotlight] deindex error: \(error.localizedDescription)") }
        }
    }

    func deindexAll() {
        CSSearchableIndex.default().deleteAllSearchableItems { error in
            if let error { print("[Spotlight] deindexAll error: \(error.localizedDescription)") }
        }
    }
}
