import SwiftUI

struct OcrTextSheet: View {
    let document: ScannedDocument
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text(document.name)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundStyle(NexarColor.foregroundSecondary)

                    // Amount & date highlights
                    if document.extractedAmount != nil || document.extractedDate != nil {
                        HStack(spacing: 10) {
                            if let amount = document.extractedAmount {
                                Label(amount, systemImage: "dollarsign.circle.fill")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundStyle(Color(hex: "155724"))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color(hex: "D4EDDA"), in: Capsule())
                            }
                            if let date = document.extractedDate {
                                Label(date, systemImage: "calendar")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundStyle(Color(hex: "0C5460"))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color(hex: "D1ECF1"), in: Capsule())
                            }
                        }
                    }

                    // OCR text
                    Text(document.ocrText.isEmpty ? "No text was extracted from this document." : document.ocrText)
                        .font(.system(size: 14, design: .monospaced))
                        .foregroundStyle(document.ocrText.isEmpty ? NexarColor.foregroundMuted : NexarColor.foregroundPrimary)
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                        )

                    if !document.ocrText.isEmpty {
                        Button {
                            UIPasteboard.general.string = document.ocrText
                            dismiss()
                        } label: {
                            Label("Copy text", systemImage: "doc.on.doc")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(NexarColor.accentPrimary, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                                .foregroundStyle(NexarColor.onAccent)
                        }
                    }
                }
                .padding(24)
            }
            .background(NexarColor.surfacePrimary.ignoresSafeArea())
            .navigationTitle("OCR Text")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Close") { dismiss() }
                        .foregroundStyle(NexarColor.accentPrimary)
                }
            }
        }
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
    }
}
