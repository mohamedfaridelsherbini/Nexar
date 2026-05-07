import Foundation

struct ScannedDocument: Identifiable, Codable, Hashable {
    let id: String
    var name: String
    let createdAt: Date
    let pageFileNames: [String]
    let pdfFileName: String

    var pageCount: Int {
        pageFileNames.count
    }
}
