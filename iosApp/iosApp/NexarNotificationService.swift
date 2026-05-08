import Foundation
import UserNotifications

private let exportReminderId = "nexar_export_reminder"

/// Handles all local notifications for the Nexar iOS app.
///
/// - Requests `UNUserNotificationCenter` permission lazily on first call.
/// - Export reminder: a single one-shot notification scheduled 24 h from the
///   last app open (rescheduled each time `scheduleExportReminder` is called
///   while pending documents exist; cancelled when the queue is clear).
/// - Duplicate alert: an immediate (trigger-less) notification posted as soon
///   as a duplicate is detected during scan processing.
actor NexarNotificationService {

    static let shared = NexarNotificationService()

    private var permissionGranted = false

    // MARK: - Permission

    /// Checks the current authorization status first so that already-granted permission
    /// is honoured on a cold launch without re-prompting the user.
    func requestPermission() async {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            permissionGranted = true
        case .notDetermined:
            do {
                permissionGranted = try await UNUserNotificationCenter.current()
                    .requestAuthorization(options: [.alert, .sound, .badge])
            } catch {
                permissionGranted = false
            }
        case .denied:
            permissionGranted = false
        @unknown default:
            permissionGranted = false
        }
    }

    // MARK: - Export reminder

    /// Schedules (or replaces) a 24-hour one-shot export reminder when `count > 0`.
    /// Cancels any pending reminder when `count == 0`.
    func scheduleExportReminder(count: Int) async {
        guard count > 0 else {
            cancelExportReminder()
            return
        }
        guard permissionGranted else { return }

        let content = UNMutableNotificationContent()
        content.title = "Documents waiting in Nexar"
        content.body = count == 1
            ? "1 document is ready to export"
            : "\(count) documents are ready to export"
        content.sound = .default

        // Replace any existing reminder with a fresh 24-hour window.
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [exportReminderId])

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 86_400, repeats: false)
        let request = UNNotificationRequest(
            identifier: exportReminderId,
            content: content,
            trigger: trigger
        )
        try? await UNUserNotificationCenter.current().add(request)
    }

    func cancelExportReminder() {
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [exportReminderId])
    }

    // MARK: - Duplicate alert

    /// Posts an immediate local notification when the OCR pipeline detects a duplicate.
    func postDuplicateAlert(docName: String, originalName: String) async {
        guard permissionGranted else { return }

        let content = UNMutableNotificationContent()
        content.title = "Possible duplicate detected"
        content.body = "'\(docName)' looks similar to '\(originalName)'"
        content.sound = .default

        let request = UNNotificationRequest(
            identifier: "nexar_duplicate_\(UUID().uuidString)",
            content: content,
            trigger: nil        // nil = deliver immediately
        )
        try? await UNUserNotificationCenter.current().add(request)
    }
}
