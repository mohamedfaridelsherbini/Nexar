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
                        // Title Stack
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
                        
                        // Local Storage Status Card
                        NexarLocalStatusCard(
                            scanCount: viewModel.filteredDocuments.count,
                            availableStorage: "2.4 GB" // Hardcoded as in .pen for design fidelity
                        )
                        
                        // Search Input
                        HStack(spacing: 10) {
                            Image(systemName: "magnifyingglass")
                                .foregroundStyle(NexarColor.foregroundMuted)
                            TextField("Search scans, tags, or dates", text: $viewModel.searchText)
                                .font(.system(size: 16, weight: .medium))
                        }
                        .padding(.horizontal, 16)
                        .frame(height: 52)
                        .background(NexarColor.surfaceSecondary, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18, style: .continuous)
                                .stroke(NexarColor.borderSubtle, lineWidth: 1)
                        )
                        
                        // Recent Documents Surface
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
                                Text("No documents yet")
                                    .foregroundStyle(NexarColor.foregroundSecondary)
                                    .padding(.top, 20)
                            } else {
                                ForEach(viewModel.filteredDocuments) { document in
                                    NexarDocumentRow(
                                        document: document,
                                        canExport: viewModel.storageFolderName != nil,
                                        onPreview: {
                                            viewModel.openPreview(for: document)
                                        },
                                        onExport: {
                                            viewModel.exportDocument(document)
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
                    .padding(.bottom, 140) // Space for bottom FAB
                }
                
                // Bottom Scan Action
                VStack {
                    Spacer()
                    Button {
                        startScanning()
                    } label: {
                        HStack(spacing: 10) {
                            Image(systemName: "viewfinder")
                                .font(.system(size: 24))
                            Text("Scan Document")
                                .font(.system(size: 18, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 64)
                        .background(NexarColor.accentPrimary, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
                        .foregroundStyle(.white)
                        .shadow(color: NexarColor.accentPrimary.opacity(0.2), radius: 18, y: 8)
                    }
                    .padding(.horizontal, 28)
                    .padding(.bottom, 34)
                }
                .ignoresSafeArea(.keyboard)
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showsFolderImporter = true
                    } label: {
                        ZStack {
                            Circle()
                                .fill(NexarColor.surfaceSecondary)
                                .frame(width: 44, height: 44)
                                .overlay(
                                    Circle().stroke(NexarColor.borderSubtle, lineWidth: 1)
                                )
                            Image(systemName: "person")
                                .foregroundStyle(NexarColor.foregroundPrimary)
                        }
                    }
                }
            }
        }
        .sheet(isPresented: $showsScanner) {
            DocumentScannerView(
                onComplete: { images in
                    showsScanner = false
                    viewModel.handleScannedImages(images)
                },
                onCancel: {
                    showsScanner = false
                }
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
            Button("Create") {
                viewModel.createFolder(named: folderName)
            }
            Button("Cancel", role: .cancel) {
            }
        } message: {
            Text("Create a subfolder inside the selected export location.")
        }
        .alert("Rename Document", isPresented: $showsRenameAlert) {
            TextField("Document name", text: $renameText)
            Button("Save") {
                guard let selectedDocument else { return }
                viewModel.renameDocument(selectedDocument, to: renameText)
            }
            Button("Cancel", role: .cancel) {
            }
        } message: {
            Text("Update the document title shown in your library.")
        }
        .alert("Scanner Unavailable", isPresented: $showsSimulatorScanAlert) {
            Button("OK", role: .cancel) {
            }
        } message: {
            Text("Document scanning requires a real iPhone or iPad. The iOS Simulator does not provide a working VisionKit camera scanner.")
        }
        .alert(
            "Error",
            isPresented: Binding(
                get: { viewModel.errorMessage != nil },
                set: { isPresented in
                    if !isPresented {
                        viewModel.errorMessage = nil
                    }
                }
            )
        ) {
            Button("OK", role: .cancel) {
                viewModel.errorMessage = nil
            }
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
