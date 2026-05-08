import Foundation

enum DuplicateDetector {
    private static let threshold = 0.75

    static func findDuplicate(newDoc: ScannedDocument, in documents: [ScannedDocument]) -> String? {
        guard !newDoc.ocrText.isEmpty else { return nil }
        let newTokens = tokenize(newDoc.ocrText)
        guard !newTokens.isEmpty else { return nil }

        var bestId: String? = nil
        var bestScore = threshold

        for doc in documents {
            guard doc.id != newDoc.id, !doc.ocrText.isEmpty else { continue }
            let score = jaccard(newTokens, tokenize(doc.ocrText))
            if score > bestScore {
                bestScore = score
                bestId = doc.id
            }
        }
        return bestId
    }

    private static func tokenize(_ text: String) -> Set<String> {
        Set(text.lowercased()
            .components(separatedBy: .whitespacesAndNewlines)
            .filter { $0.count > 2 })
    }

    private static func jaccard(_ a: Set<String>, _ b: Set<String>) -> Double {
        guard !a.isEmpty || !b.isEmpty else { return 0 }
        let intersection = a.intersection(b).count
        let union = a.union(b).count
        return union == 0 ? 0 : Double(intersection) / Double(union)
    }
}
