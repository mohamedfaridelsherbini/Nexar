import SwiftUI

enum NexarColor {
    static let accentPrimary = Color(hex: "0EA5A4")
    static let borderPrimary = Color(hex: "CBD5E1")
    static let borderSubtle = Color(hex: "E2E8F0")
    static let foregroundPrimary = Color(hex: "0F172A")
    static let foregroundSecondary = Color(hex: "475569")
    static let foregroundMuted = Color(hex: "64748B")
    static let surfacePrimary = Color(hex: "F8FAFC")
    static let surfaceSecondary = Color(hex: "FFFFFF")
    static let error = Color(hex: "B91C1C")
    static let success = Color(hex: "15803D")
    static let warning = Color(hex: "EA580C")
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

struct NexarLocalStatusCard: View {
    let scanCount: Int
    let availableStorage: String
    
    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .fill(NexarColor.accentPrimary.opacity(0.1))
                Image(systemName: "externaldrive")
                    .font(.title3)
                    .foregroundStyle(NexarColor.accentPrimary)
            }
            .frame(width: 44, height: 44)
            
            VStack(alignment: .leading, spacing: 4) {
                Text("Stored locally")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(NexarColor.foregroundPrimary)
                Text("\(scanCount) scans • \(availableStorage) available")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(NexarColor.foregroundSecondary)
            }
            
            Spacer()
            
            HStack(spacing: 6) {
                Image(systemName: "lock.fill")
                    .font(.system(size: 12))
                    .foregroundStyle(NexarColor.foregroundSecondary)
                Text("LOCAL")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(NexarColor.foregroundSecondary)
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(NexarColor.surfacePrimary, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
        }
        .padding(18)
        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .stroke(NexarColor.borderSubtle, lineWidth: 1)
        )
    }
}

struct NexarDocumentRow: View {
    let document: ScannedDocument
    let canExport: Bool
    let onPreview: () -> Void
    let onExport: () -> Void
    let onRename: () -> Void
    
    var body: some View {
        HStack(spacing: 14) {
            // PDF Preview Thumb proxy
            ZStack {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(Color.white)
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .stroke(NexarColor.borderPrimary, lineWidth: 1)
                    )
                Image(systemName: "doc.text")
                    .font(.title2)
                    .foregroundStyle(NexarColor.foregroundSecondary)
            }
            .frame(width: 62, height: 80)
            
            VStack(alignment: .leading, spacing: 6) {
                Text(document.name)
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(NexarColor.foregroundPrimary)
                
                Text("Today • \(document.pageCount) pages")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(NexarColor.foregroundSecondary)
                
                Text("Ready to export")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(NexarColor.accentPrimary)
            }
            
            Spacer()
            
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    ActionButton(icon: "eye", onClick: onPreview)
                    ActionButton(icon: "pencil", onClick: onRename)
                }
                
                ActionButton(
                    icon: canExport ? "square.and.arrow.up" : "externaldrive.badge.xmark",
                    isAccent: true,
                    onClick: onExport
                )
                .disabled(!canExport)
            }
        }
        .padding(14)
        .background(NexarColor.surfacePrimary, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .stroke(NexarColor.borderSubtle, lineWidth: 1)
        )
    }
    
    private struct ActionButton: View {
        let icon: String
        var isAccent: Bool = false
        let onClick: () -> Void
        
        var body: some View {
            Button(action: onClick) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .frame(width: 38, height: 38)
                    .background(isAccent ? NexarColor.accentPrimary.opacity(0.1) : Color.white)
                    .foregroundStyle(isAccent ? NexarColor.accentPrimary : NexarColor.foregroundSecondary)
                    .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 13, style: .continuous)
                            .stroke(NexarColor.borderSubtle, lineWidth: isAccent ? 0 : 1)
                    )
            }
            .buttonStyle(.plain)
        }
    }
}
