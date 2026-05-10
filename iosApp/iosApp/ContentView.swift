import SwiftUI
import UniformTypeIdentifiers

struct ContentView: View {
    @StateObject private var viewModel = DashboardViewModel()
    @State private var showsScanner = false
    @State private var showsFolderImporter = false
    @State private var showsCreateFolderAlert = false
    @State private var showsRenameAlert = false
    @State private var showsSimulatorScanAlert = false
    @State private var selectedDocument: ScannedDocument?
    @State private var folderName = ""
    @State private var renameText = ""
    @State private var ocrSheetDocument: ScannedDocument?
    @State private var detailDocument: ScannedDocument?
    @State private var fabPulse = false

    @ViewBuilder
    private func scanButtonLabel(isProcessing: Bool) -> some View {
        HStack(spacing: 10) {
            if isProcessing {
                ProgressView()
                    .tint(NexarColor.onAccent)
                    .scaleEffect(0.9)
                Text("Processing…")
                    .font(.system(size: 18, weight: .bold))
            } else {
                Image(systemName: "viewfinder")
                    .font(.system(size: 22, weight: .semibold))
                Text("Scan document")
                    .font(.system(size: 18, weight: .bold))
            }
        }
    }

    @ViewBuilder
    private func trailingSwipeActions(for document: ScannedDocument) -> some View {
        Button(role: .destructive) {
            viewModel.deleteDocument(document)
        } label: {
            Label("Delete", systemImage: "trash")
        }
    }

    @ViewBuilder
    private func leadingSwipeActions(for document: ScannedDocument) -> some View {
        if viewModel.storageFolderName != nil {
            Button {
                viewModel.exportDocument(document)
            } label: {
                Label("Export", systemImage: "arrow.up.doc")
            }
            .tint(NexarColor.accentPrimary)
        }
        Button {
            viewModel.toggleStar(document)
        } label: {
            Label(document.isStarred ? "Unstar" : "Star",
                  systemImage: document.isStarred ? "star.slash" : "star.fill")
        }
        .tint(.yellow)
    }

    @ViewBuilder
    private func batchExportButtonContent(isExporting: Bool, count: Int) -> some View {
        ZStack(alignment: .topTrailing) {
            Circle()
                .fill(NexarColor.surfaceSecondary)
                .frame(width: 40, height: 40)
                .overlay(Circle().stroke(NexarColor.borderSubtle, lineWidth: 1))
            if isExporting {
                ProgressView()
                    .tint(NexarColor.accentPrimary)
                    .frame(width: 40, height: 40)
            } else {
                Image(systemName: "icloud.and.arrow.up")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(NexarColor.accentPrimary)
                    .frame(width: 40, height: 40)
                Text("\(count)")
                    .font(.system(size: 9, weight: .bold))
                    .foregroundStyle(.white)
                    .padding(3)
                    .background(NexarColor.warning, in: Circle())
                    .offset(x: 2, y: -2)
            }
        }
    }

    @ViewBuilder
    private func storageSettingsButtonContent() -> some View {
        ZStack {
            Circle()
                .fill(NexarColor.surfaceSecondary)
                .frame(width: 40, height: 40)
                .overlay(Circle().stroke(NexarColor.borderSubtle, lineWidth: 1))
            Image(systemName: "person")
                .font(.system(size: 17, weight: .medium))
                .foregroundStyle(NexarColor.foregroundPrimary)
        }
    }

    @ViewBuilder
    private func documentRowView(for document: ScannedDocument) -> some View {
        let rowTransition = AnyTransition.asymmetric(
            insertion: AnyTransition.move(edge: .bottom).combined(with: .opacity),
            removal: .opacity
        )
        NexarDocumentRow(
            document: document,
            canExport: viewModel.storageFolderName != nil,
            isExporting: viewModel.exportingDocumentId == document.id,
            onPreview: { viewModel.openPreview(for: document) },
            onExport: { handleExport(document) },
            onRename: { handleRename(document) },
            onStar: { viewModel.toggleStar(document) },
            onOcrView: { ocrSheetDocument = document },
            onShare: { shareDocument(document) },
            onDetail: { detailDocument = document }
        )
        .transition(rowTransition)
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            trailingSwipeActions(for: document)
        }
        .swipeActions(edge: .leading, allowsFullSwipe: false) {
            leadingSwipeActions(for: document)
        }
    }

    var body: some View {
        NavigationStack {
            ZStack {
                NexarColor.surfacePrimary.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 18) {
                        VStack(alignment: .leading, spacing: 3) {
                            Text("Documents")
                                .font(.system(size: 34, weight: .bold))
                                .foregroundStyle(NexarColor.foregroundPrimary)
                            Text("Local scans on this iPhone")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(NexarColor.foregroundSecondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.top, 8)

                        NexarLocalStatusCard(
                            scanCount: viewModel.filteredDocuments.count,
                            availableStorage: "2.4 GB",
                            storageConfigured: viewModel.storageFolderName != nil,
                            onConfigureTap: { showsFolderImporter = true }
                        )

                        HStack(spacing: 10) {
                            Image(systemName: "magnifyingglass")
                                .font(.system(size: 18))
                                .foregroundStyle(NexarColor.foregroundMuted)
                            TextField("Search name, text, category…", text: $viewModel.searchText)
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(NexarColor.foregroundPrimary)
                                .tint(NexarColor.accentPrimary)
                        }
                        .padding(.horizontal, 16)
                        .frame(height: 52)
                        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18, style: .continuous)
                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                        )

                        HStack {
                            NexarQuickFilters(
                                selected: $viewModel.activeFilter,
                                needsExportCount: viewModel.needsExportCount
                            )
                            Spacer()
                        }

                        // Processing banner — visible while OCR/classification runs
                        if viewModel.isProcessing {
                            HStack(spacing: 10) {
                                ProgressView()
                                    .tint(NexarColor.accentPrimary)
                                Text("Processing scan…")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundStyle(NexarColor.foregroundSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .stroke(NexarColor.accentPrimary.opacity(0.3), lineWidth: 1)
                            )
                            .transition(.opacity.combined(with: .move(edge: .top)))
                        }

                        // Batch export banner
                        if viewModel.isBatchExporting {
                            HStack(spacing: 10) {
                                ProgressView()
                                    .tint(NexarColor.accentPrimary)
                                Text("Exporting all documents…")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundStyle(NexarColor.foregroundSecondary)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                            .transition(.opacity.combined(with: .move(edge: .top)))
                        }

                        VStack(spacing: 12) {
                            HStack {
                                Text("Recent scans")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundStyle(NexarColor.foregroundPrimary)
                                Spacer()
                                Text("\(viewModel.filteredDocuments.count) documents")
                                    .font(.system(size: 14, weight: .semibold))
                                    .foregroundStyle(NexarColor.foregroundSecondary)
                            }

                            if viewModel.filteredDocuments.isEmpty {
                                NexarEmptyState(
                                    filter: viewModel.activeFilter,
                                    searchText: viewModel.searchText
                                )
                                .transition(.opacity.combined(with: .scale(scale: 0.95)))
                            } else {
                                ForEach(viewModel.filteredDocuments) { document in
                                    documentRowView(for: document)
                                }
                            }
                        }
                        .padding(16)
                        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 24, style: .continuous)
                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                        )
                        .animation(.spring(response: 0.5, dampingFraction: 0.85), value: viewModel.filteredDocuments.count)
                    }
                    .padding(.horizontal, 28)
                    .padding(.bottom, 140)
                }
                .animation(.easeInOut(duration: 0.25), value: viewModel.isProcessing)
                .animation(.easeInOut(duration: 0.25), value: viewModel.isBatchExporting)

                // Floating scan button
                VStack {
                    Spacer()
                    ZStack {
                        // Attention pulse ring — only when library is empty
                        if viewModel.filteredDocuments.isEmpty && !viewModel.isProcessing {
                            RoundedRectangle(cornerRadius: 32, style: .continuous)
                                .fill(NexarColor.accentPrimary.opacity(fabPulse ? 0 : 0.35))
                                .scaleEffect(fabPulse ? 1.14 : 1.0)
                                .frame(maxWidth: .infinity)
                                .frame(height: 64)
                                .padding(.horizontal, 28)
                                .animation(
                                    .easeInOut(duration: 1.4).repeatForever(autoreverses: true),
                                    value: fabPulse
                                )
                        }
                        Button {
                            if !viewModel.isProcessing { startScanning() }
                        } label: {
                            scanButtonLabel(isProcessing: viewModel.isProcessing)
                                .frame(maxWidth: .infinity)
                                .frame(height: 64)
                                .background(
                                    NexarColor.accentPrimary.opacity(viewModel.isProcessing ? 0.7 : 1.0),
                                    in: RoundedRectangle(cornerRadius: 32, style: .continuous)
                                )
                                .foregroundStyle(NexarColor.onAccent)
                                .shadow(color: Color(hex: "0F172A").opacity(0.20), radius: 20, x: 0, y: 8)
                        }
                        .disabled(viewModel.isProcessing)
                        .padding(.horizontal, 28)
                        .padding(.bottom, 34)
                    } // ZStack
                }
                .ignoresSafeArea(.keyboard)
                .onAppear { fabPulse = true }
            }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Menu {
                        ForEach(DocumentSortOrder.allCases, id: \.self) { order in
                            Button {
                                viewModel.sortOrder = order
                            } label: {
                                if viewModel.sortOrder == order {
                                    Label(order.rawValue, systemImage: "checkmark")
                                } else {
                                    Text(order.rawValue)
                                }
                            }
                        }
                    } label: {
                        ZStack {
                            Circle()
                                .fill(NexarColor.surfaceSecondary)
                                .frame(width: 40, height: 40)
                                .overlay(Circle().stroke(NexarColor.borderSubtle, lineWidth: 1))
                            Image(systemName: "arrow.up.arrow.down")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(NexarColor.foregroundPrimary)
                        }
                    }
                    .accessibilityLabel("Sort documents")
                }

                ToolbarItem(placement: .topBarTrailing) {
                    HStack(spacing: 8) {
                        if viewModel.storageFolderName != nil && viewModel.needsExportCount > 0 {
                            Button {
                                viewModel.batchExport()
                            } label: {
                                batchExportButtonContent(isExporting: viewModel.isBatchExporting, count: viewModel.needsExportCount)
                            }
                            .disabled(viewModel.isBatchExporting)
                            .accessibilityLabel("Export \(viewModel.needsExportCount) documents to storage")
                        }

                        Button {
                            showsFolderImporter = true
                        } label: {
                            storageSettingsButtonContent()
                        }
                        .accessibilityLabel("Storage settings")
                    }
                }
            }
        }
        .tint(NexarColor.accentPrimary)
        .sheet(isPresented: $showsScanner) {
            DocumentScannerView(
                onComplete: { images in
                    showsScanner = false
                    viewModel.handleScannedImages(images)
                },
                onCancel: { showsScanner = false },
                onError: { nexarError in
                    showsScanner = false
                    viewModel.error = nexarError
                }
            )
            .ignoresSafeArea()
        }
        .sheet(item: $viewModel.previewItem) { item in
            QuickLookPreview(url: item.url)
        }
        .sheet(item: $ocrSheetDocument) { doc in
            OcrTextSheet(document: doc)
        }
        .sheet(item: $detailDocument) { doc in
            DocumentDetailView(
                document: doc,
                canExport: viewModel.storageFolderName != nil,
                onSave: { updated in viewModel.updateDocument(updated) },
                onExport: { viewModel.exportDocument($0) },
                onShare: { shareDocument($0) }
            )
        }
        .fileImporter(
            isPresented: $showsFolderImporter,
            allowedContentTypes: [.folder],
            allowsMultipleSelection: false
        ) { result in
            if case let .success(urls) = result, let url = urls.first {
                viewModel.selectStorageFolder(url)
            } else if case let .failure(importError) = result {
                viewModel.error = .exportFailed(importError.localizedDescription)
            }
        }
        .alert("Create Folder", isPresented: $showsCreateFolderAlert) {
            TextField("Folder name", text: $folderName)
            Button("Create") { viewModel.createFolder(named: folderName) }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Create a subfolder inside the selected export location.")
        }
        .alert("Rename Document", isPresented: $showsRenameAlert) {
            TextField("Document name", text: $renameText)
            Button("Save") {
                guard let selectedDocument else { return }
                viewModel.renameDocument(selectedDocument, to: renameText)
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Update the document title shown in your library.")
        }
        .alert("Scanner Unavailable", isPresented: $showsSimulatorScanAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Document scanning requires a real iPhone or iPad.")
        }
        .alert(
            "Batch Export Complete",
            isPresented: Binding(
                get: { viewModel.batchExportResult != nil },
                set: { if !$0 { viewModel.batchExportResult = nil } }
            )
        ) {
            Button("OK", role: .cancel) { viewModel.batchExportResult = nil }
        } message: {
            if let result = viewModel.batchExportResult {
                Text("\(result.success) document\(result.success != 1 ? "s" : "") exported" +
                     (result.failed > 0 ? ", \(result.failed) failed." : "."))
            }
        }
        // Typed error alert — replaces the previous generic "Error" alert
        .alert(
            viewModel.error?.alertTitle ?? "Error",
            isPresented: Binding(
                get: { viewModel.error != nil },
                set: { if !$0 { viewModel.dismissError() } }
            )
        ) {
            Button("OK", role: .cancel) { viewModel.dismissError() }
            if viewModel.error?.hasRecoverySuggestion == true {
                Button("Configure Storage") {
                    viewModel.dismissError()
                    showsFolderImporter = true
                }
            }
        } message: {
            if let err = viewModel.error {
                Text(err.errorDescription ?? "An unexpected error occurred.")
                if let suggestion = err.recoverySuggestion {
                    Text(suggestion)
                        .font(.footnote)
                }
            }
        }
    }

    private func startScanning() {
#if targetEnvironment(simulator)
        showsSimulatorScanAlert = true
#else
        showsScanner = true
#endif
    }

    private func handleExport(_ document: ScannedDocument) {
        if viewModel.storageFolderName != nil {
            viewModel.exportDocument(document)
        } else {
            showsFolderImporter = true
        }
    }

    private func handleRename(_ document: ScannedDocument) {
        selectedDocument = document
        renameText = document.name
        showsRenameAlert = true
    }

    private func shareDocument(_ document: ScannedDocument) {
        let pdfURL = FileManager.default
            .urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
            .appendingPathComponent("Nexar/pdfs/\(document.pdfFileName)")
        guard FileManager.default.fileExists(atPath: pdfURL.path) else { return }

        let activityVC = UIActivityViewController(activityItems: [pdfURL], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

#Preview {
    ContentView()
}
