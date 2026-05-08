import SwiftUI
import UIKit

// MARK: - Adaptive color tokens

/// All Nexar design tokens — automatically switch between light and dark.
enum NexarColor {
    static let accentPrimary   = Color.nexar("0EA5A4", dark: "2DD4BF")
    static let borderPrimary   = Color.nexar("CBD5E1", dark: "334155")
    static let borderSubtle    = Color.nexar("E2E8F0", dark: "1E293B")
    static let foregroundPrimary   = Color.nexar("0F172A", dark: "F8FAFC")
    static let foregroundSecondary = Color.nexar("475569", dark: "CBD5E1")
    static let foregroundMuted     = Color.nexar("64748B", dark: "94A3B8")
    static let surfacePrimary   = Color.nexar("F8FAFC", dark: "020617")
    static let surfaceSecondary = Color.nexar("FFFFFF", dark: "0F172A")
    static let surfaceElevated  = Color.nexar("FFFFFF", dark: "111827")
    static let error   = Color.nexar("B91C1C", dark: "F87171")
    static let success = Color.nexar("15803D", dark: "4ADE80")
    static let warning = Color.nexar("EA580C", dark: "FB923C")
    /// Dark forest tint used on top of the teal accent (#042F2E)
    static let onAccent = Color(hex: "042F2E")
}

extension Color {
    /// Hex-only light color (for fixed values like onAccent that never adapt).
    init(hex: String) {
        self.init(uiColor: UIColor(hexString: hex))
    }

    /// Adaptive color: switches between light and dark automatically.
    static func nexar(_ light: String, dark: String) -> Color {
        Color(uiColor: UIColor { $0.userInterfaceStyle == .dark
            ? UIColor(hexString: dark)
            : UIColor(hexString: light) })
    }
}

private extension UIColor {
    convenience init(hexString: String) {
        let hex = hexString.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3:  (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6:  (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:  (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default: (a, r, g, b) = (255, 200, 200, 200)
        }
        self.init(red: Double(r)/255, green: Double(g)/255, blue: Double(b)/255, alpha: Double(a)/255)
    }
}

// MARK: - Document Aperture mark

/// SwiftUI port of the Nexar "Document Aperture" mark from Nexar.pen.
///
/// Geometry is normalized to the pen's 188×188 field so all elements
/// scale proportionally at any `size`. Defaults to the teal app-icon treatment
/// used in document tile thumbnails.
struct NexarDocumentMark: View {
    var width: CGFloat = 62
    var height: CGFloat = 80
    var cornerRadius: CGFloat = 14
    var fieldColor: Color = NexarColor.accentPrimary
    var sheetColor: Color = NexarColor.onAccent.opacity(0.18)
    var foldColor: Color  = NexarColor.onAccent.opacity(0.28)
    var accentColor: Color = .white

    var body: some View {
        Canvas { ctx, sz in
            let u = min(sz.width, sz.height) / 188.0

            // Document sheet (76×112, r=12)
            let sheetRect = CGRect(x: 56*u, y: 38*u, width: 76*u, height: 112*u)
            let sheetPath = Path(roundedRect: sheetRect, cornerRadius: 12*u)
            ctx.fill(sheetPath, with: .color(sheetColor))

            // Fold triangle (top-right corner of sheet)
            var fold = Path()
            fold.move(to: CGPoint(x: 112*u, y: 38*u))
            fold.addLine(to: CGPoint(x: 132*u, y: 38*u))
            fold.addLine(to: CGPoint(x: 132*u, y: 58*u))
            fold.closeSubpath()
            ctx.fill(fold, with: .color(foldColor))

            // Scan beam (100×12, r=6)
            let beam = Path(roundedRect: CGRect(x: 44*u, y: 88*u, width: 100*u, height: 12*u), cornerRadius: 6*u)
            ctx.fill(beam, with: .color(accentColor))

            // Top-left bracket – horizontal
            ctx.fill(Path(roundedRect: CGRect(x: 30*u, y: 30*u, width: 36*u, height: 5*u), cornerRadius: 3*u), with: .color(accentColor))
            // Top-left bracket – vertical
            ctx.fill(Path(roundedRect: CGRect(x: 30*u, y: 30*u, width: 5*u, height: 36*u), cornerRadius: 3*u), with: .color(accentColor))
            // Bottom-right bracket – horizontal
            ctx.fill(Path(roundedRect: CGRect(x: 122*u, y: 153*u, width: 36*u, height: 5*u), cornerRadius: 3*u), with: .color(accentColor))
            // Bottom-right bracket – vertical
            ctx.fill(Path(roundedRect: CGRect(x: 153*u, y: 122*u, width: 5*u, height: 36*u), cornerRadius: 3*u), with: .color(accentColor))
        }
        .frame(width: width, height: height)
        .background(fieldColor)
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius, style: .continuous))
    }
}

// MARK: - Local storage status card

struct NexarLocalStatusCard: View {
    let scanCount: Int
    let availableStorage: String
    let storageConfigured: Bool
    let onConfigureTap: () -> Void

    private var statusSubtitle: String {
        if storageConfigured {
            let scanLabel = scanCount == 1 ? "scan" : "scans"
            return "\(scanCount) \(scanLabel) · \(availableStorage) available"
        } else {
            return "Tap to choose an export destination."
        }
    }

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .fill(NexarColor.accentPrimary.opacity(0.1))
                Image(systemName: "internaldrive")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(NexarColor.accentPrimary)
            }
            .frame(width: 44, height: 44)

            VStack(alignment: .leading, spacing: 4) {
                Text(storageConfigured ? "Stored locally" : "No export folder")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(NexarColor.foregroundPrimary)
                Text(statusSubtitle)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(NexarColor.foregroundSecondary)
            }

            Spacer()

            if storageConfigured {
                HStack(spacing: 6) {
                    Image(systemName: "lock.fill")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(NexarColor.foregroundSecondary)
                    Text("LOCAL")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(NexarColor.foregroundSecondary)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 8)
                .background(NexarColor.surfacePrimary, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
            } else {
                Button(action: onConfigureTap) {
                    Text("Set up")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(NexarColor.warning)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(NexarColor.warning.opacity(0.1), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(18)
        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .stroke(storageConfigured ? NexarColor.borderSubtle : NexarColor.warning.opacity(0.35), lineWidth: 1)
        )
    }
}

// MARK: - Document row

struct NexarDocumentRow: View {
    let document: ScannedDocument
    let canExport: Bool
    var isExporting: Bool = false
    let onPreview: () -> Void
    let onExport: () -> Void
    let onRename: () -> Void
    var onStar: (() -> Void)? = nil
    var onOcrView: (() -> Void)? = nil
    var onShare: (() -> Void)? = nil
    var onDetail: (() -> Void)? = nil

    private var statusLabel: String {
        if isExporting { return "Exporting…" }
        return document.isExportedToStorage ? "Exported" : "Ready to export"
    }
    private var statusColor: Color {
        if isExporting { return NexarColor.accentPrimary }
        return document.isExportedToStorage ? NexarColor.success : NexarColor.accentPrimary
    }

    var body: some View {
        HStack(spacing: 14) {
            NexarDocumentMark()

            VStack(alignment: .leading, spacing: 5) {
                HStack {
                    Text(document.name)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(NexarColor.foregroundPrimary)
                        .lineLimit(1)
                    Spacer()
                    if let onStar {
                        Button(action: onStar) {
                            Image(systemName: document.isStarred ? "star.fill" : "star")
                                .font(.system(size: 14))
                                .foregroundStyle(document.isStarred ? Color.yellow : NexarColor.foregroundMuted)
                        }
                        .buttonStyle(.plain)
                    }
                }

                let pages = document.pageCount
                Text("\(pages) \(pages == 1 ? "page" : "pages")")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(NexarColor.foregroundSecondary)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        Text(statusLabel)
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(statusColor)

                        if document.category != .other {
                            CategoryPill(category: document.category)
                        }

                        if let amount = document.extractedAmount {
                            Text(amount)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundStyle(Color(hex: "155724"))
                                .padding(.horizontal, 7).padding(.vertical, 2)
                                .background(Color(hex: "D4EDDA"), in: Capsule())
                        }

                        if document.ocrProcessed {
                            let ocrLabel = document.ocrText.isEmpty ? "No text" : "Text found"
                            let ocrColor = document.ocrText.isEmpty ? NexarColor.foregroundMuted : NexarColor.success
                            Text("· \(ocrLabel)")
                                .font(.system(size: 11, weight: .medium))
                                .foregroundStyle(ocrColor)
                        }

                        if document.duplicateOfId != nil {
                            Label("Duplicate", systemImage: "doc.on.doc")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundStyle(NexarColor.warning)
                        }
                    }
                }

                // Action buttons
                HStack(spacing: 6) {
                    SmallActionButton(icon: "eye", onClick: onPreview)
                    SmallActionButton(icon: "pencil", onClick: onRename)
                    if canExport {
                        if isExporting {
                            ProgressView()
                                .scaleEffect(0.8)
                                .frame(width: 30, height: 28)
                        } else {
                            SmallActionButton(icon: document.isExportedToStorage ? "checkmark.circle" : "icloud.and.arrow.up",
                                              isAccent: true, onClick: onExport)
                        }
                    }
                    if let onOcrView, document.ocrProcessed && !document.ocrText.isEmpty {
                        SmallActionButton(icon: "text.viewfinder", onClick: onOcrView)
                    }
                    if let onShare {
                        SmallActionButton(icon: "square.and.arrow.up", onClick: onShare)
                    }
                    if let onDetail {
                        SmallActionButton(icon: "info.circle", onClick: onDetail)
                    }
                }
                .padding(.top, 4)
            }
        }
        .padding(14)
        .background(NexarColor.surfacePrimary, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(NexarColor.borderSubtle, lineWidth: 1)
        )
    }
}

private struct SmallActionButton: View {
    let icon: String
    var isAccent: Bool = false
    var isWarning: Bool = false
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            Image(systemName: icon)
                .font(.system(size: 13, weight: .medium))
                .frame(width: 30, height: 28)
                .background(
                    isAccent ? NexarColor.accentPrimary.opacity(0.1) :
                    isWarning ? NexarColor.warning.opacity(0.1) : NexarColor.surfaceSecondary,
                    in: RoundedRectangle(cornerRadius: 8, style: .continuous)
                )
                .foregroundStyle(isAccent ? NexarColor.accentPrimary : isWarning ? NexarColor.warning : NexarColor.foregroundSecondary)
                .overlay(
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .stroke(NexarColor.borderSubtle, lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Category pill

struct CategoryPill: View {
    let category: DocumentCategory

    private var colors: (bg: Color, fg: Color) {
        switch category {
        case .receipt:    return (Color(hex: "FFF3CD"), Color(hex: "856404"))
        case .invoice:    return (Color(hex: "D1ECF1"), Color(hex: "0C5460"))
        case .idDocument: return (Color(hex: "D4EDDA"), Color(hex: "155724"))
        case .contract:   return (Color(hex: "E2D9F3"), Color(hex: "4A235A"))
        case .medical:    return (Color(hex: "FFE0E0"), Color(hex: "7B1818"))
        case .other:      return (NexarColor.borderSubtle, NexarColor.foregroundMuted)
        }
    }

    var body: some View {
        if category == .other { EmptyView() }
        else {
            Text(category.rawValue)
                .font(.system(size: 11, weight: .semibold))
                .foregroundStyle(colors.fg)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(colors.bg, in: Capsule())
        }
    }
}

// MARK: - Quick filter chips

struct NexarQuickFilters: View {
    @Binding var selected: DocumentListFilter
    let needsExportCount: Int

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                FilterChip(label: "All", selected: selected == .all) { selected = .all }

                FilterChip(
                    label: needsExportCount > 0 ? "Needs export · \(needsExportCount)" : "Needs export",
                    selected: selected == .needsExport,
                    accent: NexarColor.warning,
                    selectedFg: NexarColor.onAccent
                ) { selected = .needsExport }

                FilterChip(label: "⭐ Starred", selected: selected == .starred)    { selected = .starred }
                FilterChip(label: "Receipts",  selected: selected == .receipt)   { selected = .receipt }
                FilterChip(label: "Invoices",  selected: selected == .invoice)   { selected = .invoice }
                FilterChip(label: "IDs",       selected: selected == .idDocument){ selected = .idDocument }
                FilterChip(label: "Contracts", selected: selected == .contract)  { selected = .contract }
                FilterChip(label: "Medical",   selected: selected == .medical)   { selected = .medical }
            }
            .padding(.horizontal, 4)
        }
    }
}

private struct FilterChip: View {
    let label: String
    let selected: Bool
    var accent: Color = NexarColor.accentPrimary
    var selectedFg: Color = NexarColor.onAccent
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(selected ? selectedFg : NexarColor.foregroundSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(
                    selected
                        ? accent
                        : NexarColor.surfaceSecondary,
                    in: Capsule()
                )
                .overlay(
                    Capsule()
                        .stroke(selected ? Color.clear : NexarColor.borderSubtle, lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Empty state

struct NexarEmptyState: View {
    let filter: DocumentListFilter
    let searchText: String

    private var heading: String {
        if !searchText.isEmpty { return "No matches found" }
        if filter == .needsExport { return "Everything exported" }
        if filter == .starred { return "No starred documents" }
        if let cat = filter.category { return "No \(cat.folderName.lowercased()) found" }
        return "No documents yet"
    }
    private var body_: String {
        if !searchText.isEmpty { return "OCR text, category, and document name are all searched." }
        if filter == .needsExport { return "All your scans have been exported." }
        if filter == .starred { return "Tap the star on any document to mark it as a favourite." }
        if filter.category != nil { return "Scan a document — Nexar will auto-detect the category." }
        return "Tap Scan document below to capture your first scan."
    }

    var body: some View {
        VStack(spacing: 16) {
            NexarDocumentMark(
                width: 72, height: 72,
                cornerRadius: 22,
                fieldColor: NexarColor.foregroundPrimary.opacity(0.06),
                sheetColor: NexarColor.surfaceSecondary,
                foldColor: NexarColor.borderSubtle,
                accentColor: NexarColor.accentPrimary.opacity(0.45)
            )
            Text(heading)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(NexarColor.foregroundPrimary)
            Text(body_)
                .font(.system(size: 14, weight: .regular))
                .foregroundStyle(NexarColor.foregroundSecondary)
                .multilineTextAlignment(.center)
        }
        .padding(.vertical, 28)
        .frame(maxWidth: .infinity)
    }
}
