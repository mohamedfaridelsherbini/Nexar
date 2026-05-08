import Foundation

/// Typed error domain for the Nexar iOS app.
/// Mirrors the KMP `NexarError` sealed class in `commonMain`.
enum NexarError: LocalizedError, Identifiable, Equatable {
    case scannerFailed(String)
    case ocrFailed(String)
    case exportFailed(String)
    case folderCreationFailed
    case previewUnavailable

    // MARK: - Identifiable

    var id: String {
        switch self {
        case .scannerFailed(let s):  return "scanner_\(s)"
        case .ocrFailed(let n):      return "ocr_\(n)"
        case .exportFailed(let n):   return "export_\(n)"
        case .folderCreationFailed:  return "folder"
        case .previewUnavailable:    return "preview"
        }
    }

    // MARK: - LocalizedError

    var errorDescription: String? {
        switch self {
        case .scannerFailed(let detail):
            return "Scanning failed: \(detail)"
        case .ocrFailed(let name):
            return "Could not process '\(name)'. Please try again."
        case .exportFailed(let name):
            return "Export failed for '\(name)'. Check your storage folder."
        case .folderCreationFailed:
            return "Could not create folder. Check your storage permissions."
        case .previewUnavailable:
            return "No preview file is available for this document."
        }
    }

    var recoverySuggestion: String? {
        switch self {
        case .exportFailed, .folderCreationFailed:
            return "Tap the folder icon to re-configure your storage location."
        case .ocrFailed:
            return "Re-scan the document for better results."
        default:
            return nil
        }
    }

    // Convenience for alert titles
    var alertTitle: String {
        switch self {
        case .scannerFailed:      return "Scanner Error"
        case .ocrFailed:          return "Processing Failed"
        case .exportFailed:       return "Export Failed"
        case .folderCreationFailed: return "Folder Error"
        case .previewUnavailable: return "Preview Unavailable"
        }
    }

    var hasRecoverySuggestion: Bool { recoverySuggestion != nil }
}
