import Foundation

enum DocumentExtractor {

    static func extractAmount(from text: String) -> String? {
        guard !text.isEmpty else { return nil }
        let patterns = [
            #"[$£€]\s*[\d,]+\.?\d*"#,
            #"[\d,]+\.\d{2}\s*[$£€]"#,
            #"(?i)total[:\s]+[$£€]?\s*([\d,]+\.?\d*)"#,
            #"(?i)amount[:\s]+[$£€]?\s*([\d,]+\.?\d*)"#,
            #"[\d]{1,3}(?:,\d{3})*\.\d{2}"#
        ]

        var candidates: [(String, Double)] = []
        for pattern in patterns {
            guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) else { continue }
            let range = NSRange(text.startIndex..., in: text)
            let matches = regex.matches(in: text, range: range)
            for match in matches {
                guard let range = Range(match.range, in: text) else { continue }
                let raw = String(text[range]).trimmingCharacters(in: .whitespaces)
                let numeric = raw.components(separatedBy: CharacterSet(charactersIn: "0123456789.").inverted).joined()
                if let value = Double(numeric) {
                    candidates.append((raw, value))
                }
            }
        }
        return candidates.max(by: { $0.1 < $1.1 })?.0
    }

    static func extractDate(from text: String) -> String? {
        guard !text.isEmpty else { return nil }
        let patterns = [
            #"\d{1,2}/\d{1,2}/\d{2,4}"#,
            #"\d{4}-\d{2}-\d{2}"#,
            #"\d{1,2}-\d{1,2}-\d{2,4}"#,
            #"(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{1,2},?\s+\d{4}"#,
            #"(?i)\d{1,2}\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\.?\s+\d{4}"#
        ]

        for pattern in patterns {
            guard let regex = try? NSRegularExpression(pattern: pattern, options: .caseInsensitive) else { continue }
            let range = NSRange(text.startIndex..., in: text)
            if let match = regex.firstMatch(in: text, range: range),
               let matchRange = Range(match.range, in: text) {
                return String(text[matchRange]).trimmingCharacters(in: .whitespaces)
            }
        }
        return nil
    }
}
