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
            List {
                if viewModel.storageFolderName == nil {
                    Section {
                        StorageBanner(
                            title: "Storage location not set",
                            message: "Choose a folder to export PDFs to Files or iCloud Drive.",
                            tint: .orange
                        )
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                    }
                } else {
                    Section {
                        StorageBanner(
                            title: "Export folder",
                            message: viewModel.storageFolderName ?? "",
                            tint: .blue
                        )
                        .listRowInsets(EdgeInsets())
                        .listRowBackground(Color.clear)
                    }
                }

                Section("Recent Scans") {
                    if viewModel.filteredDocuments.isEmpty {
                        ContentUnavailableView(
                            "No documents yet",
                            systemImage: "doc.text.viewfinder",
                            description: Text("Scan a document to build your local library.")
                        )
                        .frame(maxWidth: .infinity, alignment: .center)
                    } else {
                        ForEach(viewModel.filteredDocuments) { document in
                            DocumentRow(
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
                        .onDelete(perform: viewModel.deleteDocuments)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Nexar")
            .searchable(text: $viewModel.searchText, prompt: "Search your documents")
            .toolbar {
                ToolbarItemGroup(placement: .topBarTrailing) {
                    if viewModel.storageFolderName != nil {
                        Button {
                            folderName = ""
                            showsCreateFolderAlert = true
                        } label: {
                            Image(systemName: "folder.badge.plus")
                        }
                    }

                    Button {
                        showsFolderImporter = true
                    } label: {
                        Image(systemName: "externaldrive.badge.plus")
                    }
                }
            }
            .safeAreaInset(edge: .bottom) {
                HStack {
                    Spacer()

                    Button {
                        startScanning()
                    } label: {
                        Label("Scan", systemImage: "doc.viewfinder")
                            .font(.headline)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                            .background(Color.accentColor, in: Capsule())
                            .foregroundStyle(.white)
                            .shadow(color: Color.black.opacity(0.14), radius: 18, y: 8)
                    }
                    .padding(.trailing, 20)
                    .padding(.top, 8)
                }
                .background(Color.clear)
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

private struct StorageBanner: View {
    let title: String
    let message: String
    let tint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.headline)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(tint.opacity(0.12), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(tint.opacity(0.18), lineWidth: 1)
        )
        .padding(.vertical, 4)
    }
}

private struct DocumentRow: View {
    let document: ScannedDocument
    let canExport: Bool
    let onPreview: () -> Void
    let onExport: () -> Void
    let onRename: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .fill(Color.accentColor.opacity(0.12))
                .frame(width: 52, height: 52)
                .overlay {
                    Image(systemName: "doc.richtext")
                        .font(.title3)
                        .foregroundStyle(Color.accentColor)
                }

            VStack(alignment: .leading, spacing: 4) {
                Text(document.name)
                    .font(.headline)
                    .lineLimit(1)

                Text("\(document.pageCount) page\(document.pageCount == 1 ? "" : "s")")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            HStack(spacing: 4) {
                Button(action: onExport) {
                    Image(systemName: canExport ? "square.and.arrow.up" : "externaldrive.badge.xmark")
                        .foregroundStyle(canExport ? Color.accentColor : .secondary)
                }
                .buttonStyle(.plain)
                .disabled(!canExport)

                Button(action: onRename) {
                    Image(systemName: "pencil")
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onPreview)
    }
}

#Preview {
    ContentView()
}
