import SwiftUI

struct SettingsView: View {
    @StateObject private var viewModel = SettingsViewModel()
    @Environment(\.dismiss) private var dismiss
    @State private var showsThemePicker = false
    @State private var showsFolderImporter = false

    var body: some View {
        NavigationStack {
            List {
                Section("Appearance") {
                    Button {
                        showsThemePicker = true
                    } label: {
                        HStack {
                            Label("Theme", systemImage: "paintbrush")
                                .foregroundStyle(NexarColor.foregroundPrimary)
                            Spacer()
                            Text(viewModel.appTheme)
                                .foregroundStyle(NexarColor.foregroundSecondary)
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundStyle(NexarColor.borderPrimary)
                        }
                    }
                }

                Section("Storage") {
                    Button {
                        showsFolderImporter = true
                    } label: {
                        HStack {
                            Label("Export location", systemImage: "folder")
                                .foregroundStyle(NexarColor.foregroundPrimary)
                            Spacer()
                            Text(viewModel.storageFolderName ?? "Not configured")
                                .foregroundStyle(NexarColor.foregroundSecondary)
                                .lineLimit(1)
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundStyle(NexarColor.borderPrimary)
                        }
                    }
                }

                Section("Notifications") {
                    Toggle(isOn: Binding(
                        get: { viewModel.exportRemindersEnabled },
                        set: { viewModel.toggleExportReminders($0) }
                    )) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Export reminders")
                                .font(.system(size: 17, weight: .semibold))
                            Text("Alert me about unsaved scans")
                                .font(.system(size: 13))
                                .foregroundStyle(NexarColor.foregroundSecondary)
                        }
                    }
                    .tint(NexarColor.accentPrimary)

                    Toggle(isOn: Binding(
                        get: { viewModel.duplicateAlertsEnabled },
                        set: { viewModel.toggleDuplicateAlerts($0) }
                    )) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Duplicate alerts")
                                .font(.system(size: 17, weight: .semibold))
                            Text("Notify when a duplicate is found")
                                .font(.system(size: 13))
                                .foregroundStyle(NexarColor.foregroundSecondary)
                        }
                    }
                    .tint(NexarColor.accentPrimary)
                }

                Section("About") {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text(viewModel.version)
                            .foregroundStyle(NexarColor.foregroundSecondary)
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
            .confirmationDialog("Choose theme", isPresented: $showsThemePicker, titleVisibility: .visible) {
                Button("System") { viewModel.updateTheme("System") }
                Button("Light") { viewModel.updateTheme("Light") }
                Button("Dark") { viewModel.updateTheme("Dark") }
                Button("Cancel", role: .cancel) {}
            }
            .fileImporter(
                isPresented: $showsFolderImporter,
                allowedContentTypes: [.folder],
                allowsMultipleSelection: false
            ) { result in
                if case let .success(urls) = result, let url = urls.first {
                    Task {
                        await viewModel.setStorageFolder(url)
                    }
                }
            }
        }
    }
}

#Preview {
    SettingsView()
}
