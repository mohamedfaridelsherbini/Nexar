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

    var body: some View {
        NavigationStack {
            ZStack {
                NexarColor.surfacePrimary.ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 18) {
                        // Title stack — matches pen's "Dashboard Title" + "Dashboard Subtitle"
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

                        // Storage status card — warning state when no folder
                        NexarLocalStatusCard(
                            scanCount: viewModel.filteredDocuments.count,
                            availableStorage: "2.4 GB",
                            storageConfigured: viewModel.storageFolderName != nil,
                            onConfigureTap: { showsFolderImporter = true }
                        )

                        // Search input
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

                        // Quick filters
                        HStack {
                            NexarQuickFilters(
                                selected: $viewModel.activeFilter,
                                needsExportCount: viewModel.needsExportCount
                            )
                            Spacer()
                        }

                        // Recent documents surface
                        VStack(spacing: 12) {
                            // List header
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
                            } else {
                                ForEach(viewModel.filteredDocuments) { document in
                                    NexarDocumentRow(
                                        document: document,
                                        canExport: viewModel.storageFolderName != nil,
                                        onPreview: {
                                            viewModel.openPreview(for: document)
                                        },
                                        onExport: {
                                            if viewModel.storageFolderName != nil {
                                                viewModel.exportDocument(document)
                                            } else {
                                                showsFolderImporter = true
                                            }
                                        },
                                        onRename: {
                                            selectedDocument = document
                                            renameText = document.name
                                            showsRenameAlert = true
                                        }
                                    )
                                }
                            }
                        }
                        .padding(16)
                        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 24, style: .continuous)
                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                        )
                    }
                    .padding(.horizontal, 28)
                    .padding(.bottom, 140)
                }

                // Dominant scan FAB — pen spec: h=64, r=32, teal, shadow y=8 blur=20 navy 20%
                VStack {
                    Spacer()
                    Button {
                        startScanning()
                    } label: {
                        HStack(spacing: 10) {
                            Image(systemName: "viewfinder")
                                .font(.system(size: 22, weight: .semibold))
                            Text("Scan document")
                                .font(.system(size: 18, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 64)
                        .background(NexarColor.accentPrimary, in: RoundedRectangle(cornerRadius: 32, style: .continuous))
                        .foregroundStyle(NexarColor.onAccent)
                        .shadow(color: Color(hex: "0F172A").opacity(0.20), radius: 20, x: 0, y: 8)
                    }
                    .padding(.horizontal, 28)
                    .padding(.bottom, 34)
                }
                .ignoresSafeArea(.keyboard)
            }
            .toolbar {
                // Profile / settings button — pen: 44×44 circle, foreground-primary icon
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showsFolderImporter = true
                    } label: {
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
                onCancel: { showsScanner = false }
            )
            .ignoresSafeArea()
        }
        .sheet(item: $viewModel.previewItem) { item in
            QuickLookPreview(url: item.url)
        }
        .fileImporter(
            isPresented: $showsFolderImporter,
            allowedContentTypes: [.folder],
            allowsMultipleSelection: false
        ) { result in
            if case let .success(urls) = result, let url = urls.first {
                viewModel.selectStorageFolder(url)
            } else if case let .failure(error) = result {
                viewModel.errorMessage = error.localizedDescription
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
            "Error",
            isPresented: Binding(
                get: { viewModel.errorMessage != nil },
                set: { if !$0 { viewModel.errorMessage = nil } }
            )
        ) {
            Button("OK", role: .cancel) { viewModel.errorMessage = nil }
        } message: {
            Text(viewModel.errorMessage ?? "")
        }
    }

    private func startScanning() {
#if targetEnvironment(simulator)
        showsSimulatorScanAlert = true
#else
        showsScanner = true
#endif
    }
}

#Preview {
    ContentView()
}
