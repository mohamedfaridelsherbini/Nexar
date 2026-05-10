import Foundation
import SwiftUI

@MainActor
final class SettingsViewModel: ObservableObject {
    @Published var appTheme: String = "System"
    @Published var exportRemindersEnabled: Bool = true
    @Published var duplicateAlertsEnabled: Bool = true
    @Published var storageFolderName: String?

    private let defaults = UserDefaults.standard
    private let repository: DocumentRepository

    // Keys must match NexarPrefs.ios.kt
    private let KEY_THEME = "app_theme"
    private let KEY_EXPORT_REMINDERS = "export_reminders_enabled"
    private let KEY_DUPLICATE_ALERTS = "duplicate_alerts_enabled"

    init(repository: DocumentRepository = DocumentStore()) {
        self.repository = repository
        self.load()
    }

    func load() {
        self.appTheme = defaults.string(forKey: KEY_THEME) ?? "System"

        // Default to true if not set
        if defaults.object(forKey: KEY_EXPORT_REMINDERS) == nil {
            self.exportRemindersEnabled = true
        } else {
            self.exportRemindersEnabled = defaults.bool(forKey: KEY_EXPORT_REMINDERS)
        }

        if defaults.object(forKey: KEY_DUPLICATE_ALERTS) == nil {
            self.duplicateAlertsEnabled = true
        } else {
            self.duplicateAlertsEnabled = defaults.bool(forKey: KEY_DUPLICATE_ALERTS)
        }

        Task {
            self.storageFolderName = await repository.storageFolderName()
        }
    }

    func updateTheme(_ theme: String) {
        defaults.set(theme, forKey: KEY_THEME)
        self.appTheme = theme
    }

    func toggleExportReminders(_ enabled: Bool) {
        defaults.set(enabled, forKey: KEY_EXPORT_REMINDERS)
        self.exportRemindersEnabled = enabled
    }

    func toggleDuplicateAlerts(_ enabled: Bool) {
        defaults.set(enabled, forKey: KEY_DUPLICATE_ALERTS)
        self.duplicateAlertsEnabled = enabled
    }

    func setStorageFolder(_ url: URL) async {
        do {
            _ = try await repository.setStorageFolder(url)
            self.storageFolderName = await repository.storageFolderName()
        } catch {
            print("Failed to set storage folder in settings: \(error)")
        }
    }

    var version: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }
}
