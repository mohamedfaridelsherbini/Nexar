import Foundation

enum DuplicateDetector {
    private static let threshold = 0.75

    static func findDuplicate(newDoc: ScannedDocument, in documents: [ScannedDocument]) -> String? {
        guard !newDoc.ocrText.isEmpty else { return nil }
        let newTokens = tokenize(newDoc.ocrText)
        guard !newTokens.isEmpty else { return nil }

        var bestId: String?
        var bestScore = threshold

        for doc in documents {
            guard doc.id != newDoc.id, !doc.ocrText.isEmpty else { continue }
            let score = jaccard(lhs: newTokens, rhs: tokenize(doc.ocrText))
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

    private static func jaccard(lhs: Set<String>, rhs: Set<String>) -> Double {
        guard !lhs.isEmpty || !rhs.isEmpty else { return 0 }
        let intersection = lhs.intersection(rhs).count
        let union = lhs.union(rhs).count
        return union == 0 ? 0 : Double(intersection) / Double(union)
    }
}
