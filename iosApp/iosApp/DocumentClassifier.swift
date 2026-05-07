import Foundation

enum DocumentClassifier {
    static func classify(_ ocrText: String) -> (DocumentCategory, [String]) {
        guard !ocrText.isEmpty else { return (.other, []) }
        let lower = ocrText.lowercased()

        let scores: [(DocumentCategory, [String])] = [
            (.receipt,    ["receipt", "subtotal", "cashier", "store", "thank you for shopping",
                           "change", "loyalty", "carrefour", "walmart", "grocery", "amount paid"]),
            (.invoice,    ["invoice", "bill to", "billing address", "due date", "invoice number",
                           "inv #", "vat", "net amount", "unit price", "balance due"]),
            (.idDocument, ["passport", "national id", "identity card", "driver's license",
                           "driving licence", "date of birth", "nationality", "id number",
                           "personal no", "expiry date", "mrz", "bearer"]),
            (.contract,   ["agreement", "contract", "terms and conditions", "hereby agrees",
                           "binding agreement", "whereas", "in witness whereof", "hereinafter",
                           "indemnification", "arbitration", "governing law", "clause"]),
            (.medical,    ["patient", "diagnosis", "prescription", "doctor", "physician",
                           "hospital", "clinic", "medication", "dosage", "treatment",
                           "lab result", "blood pressure", "medical record"])
        ]

        var bestCategory = DocumentCategory.other
        var bestScore = 0
        var tags: [String] = []

        for (category, keywords) in scores {
            let score = keywords.filter { lower.contains($0) }.count
            if score >= 1 { tags.append(category.rawValue.lowercased()) }
            if score >= 2 && score > bestScore {
                bestScore = score
                bestCategory = category
            }
        }

        return (bestCategory, tags)
    }
}

enum DocumentNamer {
    static func suggest(ocrText: String, category: DocumentCategory, date: Date) -> String? {
        guard !ocrText.isEmpty, category != .other else { return nil }
        let dateStr = formatDate(date)
        let excerpt = extractExcerpt(from: ocrText)
        let prefix = category.rawValue
        if let e = excerpt {
            return "\(prefix) - \(e) - \(dateStr)"
        }
        return "\(prefix) - \(dateStr)"
    }

    private static func extractExcerpt(from text: String) -> String? {
        let lines = text.components(separatedBy: "\n")
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { $0.count >= 3 && $0.count <= 40 }
        return lines.first { line in
            line.contains(where: { $0.isLetter }) &&
            !line.allSatisfy { $0.isNumber || $0.isWhitespace || $0 == "/" || $0 == "-" }
        }.map { String($0.prefix(30)) }
    }

    private static func formatDate(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        return f.string(from: date)
    }
}
