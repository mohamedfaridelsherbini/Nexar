import Foundation

enum DocumentCategory: String, Codable, CaseIterable, Hashable {
    case receipt    = "Receipt"
    case invoice    = "Invoice"
    case idDocument = "ID"
    case contract   = "Contract"
    case medical    = "Medical"
    case other      = "Other"

    var folderName: String {
        switch self {
        case .receipt:    return "Receipts"
        case .invoice:    return "Invoices"
        case .idDocument: return "IDs"
        case .contract:   return "Contracts"
        case .medical:    return "Medical"
        case .other:      return "Other"
        }
    }
}

struct ScannedDocument: Identifiable, Codable, Hashable {
    let id: String
    var name: String
    let createdAt: Date
    let pageFileNames: [String]
    let pdfFileName: String
    var isExportedToStorage: Bool = false
    var ocrText: String = ""
    var category: DocumentCategory = .other
    var tags: [String] = []
    var ocrProcessed: Bool = false
    var isStarred: Bool = false
    var extractedAmount: String?
    var extractedDate: String?
    var duplicateOfId: String?

    var pageCount: Int { pageFileNames.count }
}
