import SwiftUI

struct DocumentDetailView: View {
    let document: ScannedDocument
    let canExport: Bool
    let onSave: (ScannedDocument) -> Void
    let onExport: (ScannedDocument) -> Void
    let onShare: ((ScannedDocument) -> Void)?

    @Environment(\.dismiss) private var dismiss
    @State private var name: String
    @State private var category: DocumentCategory
    @State private var tags: [String]
    @State private var tagInput = ""
    @State private var showCategoryPicker = false

    private var isDirty: Bool {
        name != document.name || category != document.category || tags != document.tags
    }

    init(document: ScannedDocument, canExport: Bool,
         onSave: @escaping (ScannedDocument) -> Void,
         onExport: @escaping (ScannedDocument) -> Void,
         onShare: ((ScannedDocument) -> Void)?) {
        self.document = document
        self.canExport = canExport
        self.onSave = onSave
        self.onExport = onExport
        self.onShare = onShare
        _name = State(initialValue: document.name)
        _category = State(initialValue: document.category)
        _tags = State(initialValue: document.tags)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // Thumbnail strip
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            let count = max(1, document.pageCount)
                            ForEach(0..<count, id: \.self) { i in
                                VStack(spacing: 4) {
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(NexarColor.surfaceSecondary)
                                        .frame(width: 88, height: 120)
                                        .overlay(
                                            NexarDocumentMark(width: 36, height: 48)
                                                .opacity(0.7)
                                        )
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                                        )
                                    Text("Page \(i + 1)")
                                        .font(.system(size: 11))
                                        .foregroundStyle(NexarColor.foregroundMuted)
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }

                    // Name field
                    DetailSection(title: "Document name") {
                        TextField("Document name", text: $name)
                            .font(.system(size: 16, weight: .medium))
                            .padding(14)
                            .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .stroke(NexarColor.borderSubtle, lineWidth: 1)
                            )
                    }

                    // Category picker
                    DetailSection(title: "Category") {
                        Menu {
                            ForEach(DocumentCategory.allCases, id: \.self) { cat in
                                Button {
                                    category = cat
                                } label: {
                                    if cat == category {
                                        Label(cat.rawValue, systemImage: "checkmark")
                                    } else {
                                        Text(cat.rawValue)
                                    }
                                }
                            }
                        } label: {
                            HStack {
                                Text(category.rawValue)
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundStyle(NexarColor.foregroundPrimary)
                                Spacer()
                                Image(systemName: "chevron.down")
                                    .font(.system(size: 13))
                                    .foregroundStyle(NexarColor.foregroundSecondary)
                            }
                            .padding(14)
                            .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .stroke(NexarColor.borderSubtle, lineWidth: 1)
                            )
                        }
                    }

                    // Tags
                    DetailSection(title: "Tags") {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                TextField("Add a tag…", text: $tagInput)
                                    .font(.system(size: 16, weight: .medium))
                                    .padding(12)
                                    .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                                            .stroke(NexarColor.borderSubtle, lineWidth: 1)
                                    )
                                Button {
                                    let t = tagInput.trimmingCharacters(in: .whitespaces)
                                    if !t.isEmpty && !tags.contains(t) {
                                        tags.append(t)
                                        tagInput = ""
                                    }
                                } label: {
                                    Image(systemName: "plus")
                                        .font(.system(size: 16, weight: .semibold))
                                        .frame(width: 40, height: 40)
                                        .background(NexarColor.accentPrimary, in: Circle())
                                        .foregroundStyle(NexarColor.onAccent)
                                }
                            }
                            if !tags.isEmpty {
                                FlowLayout(tags: tags, onRemove: { tag in
                                    tags.removeAll { $0 == tag }
                                })
                            }
                        }
                    }

                    // Extracted data
                    if document.ocrProcessed {
                        DetailSection(title: "Extracted data") {
                            VStack(spacing: 6) {
                                if let amount = document.extractedAmount {
                                    InfoRow(label: "Amount", value: amount)
                                }
                                if let date = document.extractedDate {
                                    InfoRow(label: "Date", value: date)
                                }
                                let year = Calendar.current.component(.year, from: document.createdAt)
                                InfoRow(label: "Export path", value: "\(document.category.folderName)/\(year)")
                            }
                        }
                    }

                    // Actions
                    HStack(spacing: 10) {
                        if canExport {
                            Button {
                                onExport(document)
                                dismiss()
                            } label: {
                                Label(document.isExportedToStorage ? "Re-export" : "Export",
                                      systemImage: document.isExportedToStorage ? "checkmark.circle" : "icloud.and.arrow.up")
                                    .font(.system(size: 15, weight: .semibold))
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 50)
                                    .background(NexarColor.accentPrimary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                                    .foregroundStyle(NexarColor.onAccent)
                            }
                        }
                        if let onShare {
                            Button {
                                onShare(document)
                            } label: {
                                Label("Share", systemImage: "square.and.arrow.up")
                                    .font(.system(size: 15, weight: .semibold))
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 50)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                                            .stroke(NexarColor.borderSubtle, lineWidth: 1)
                                    )
                                    .foregroundStyle(NexarColor.foregroundPrimary)
                            }
                        }
                    }
                }
                .padding(24)
            }
            .background(NexarColor.surfacePrimary.ignoresSafeArea())
            .navigationTitle("Document Detail")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Close") { dismiss() }
                        .foregroundStyle(NexarColor.foregroundSecondary)
                }
                if isDirty {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Save") {
                            var updated = document
                            updated.name = name.trimmingCharacters(in: .whitespaces).isEmpty ? document.name : name.trimmingCharacters(in: .whitespaces)
                            updated.category = category
                            updated.tags = tags
                            onSave(updated)
                            dismiss()
                        }
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(NexarColor.accentPrimary)
                    }
                }
            }
        }
    }
}

// MARK: - Sub-components

private struct DetailSection<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(NexarColor.foregroundSecondary)
                .textCase(.uppercase)
                .kerning(0.5)
            content()
        }
    }
}

private struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 14))
                .foregroundStyle(NexarColor.foregroundSecondary)
            Spacer()
            Text(value)
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(NexarColor.foregroundPrimary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .stroke(NexarColor.borderSubtle, lineWidth: 1)
        )
    }
}

private struct FlowLayout: View {
    let tags: [String]
    let onRemove: (String) -> Void

    var body: some View {
        // Simple wrapping row using flexible layout
        let width: CGFloat = 0
        let _ = width // suppress warning

        HStack(spacing: 6) {
            ForEach(tags, id: \.self) { tag in
                HStack(spacing: 4) {
                    Text(tag)
                        .font(.system(size: 13, weight: .medium))
                    Button { onRemove(tag) } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 10, weight: .bold))
                    }
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(NexarColor.surfaceSecondary, in: Capsule())
                .overlay(Capsule().stroke(NexarColor.borderSubtle, lineWidth: 1))
                .foregroundStyle(NexarColor.foregroundPrimary)
            }
        }
        .flexibleFrame()
    }
}

private extension View {
    func flexibleFrame() -> some View {
        self.frame(maxWidth: .infinity, alignment: .leading)
            .lineLimit(nil)
    }
}
