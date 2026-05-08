import Foundation
import UserNotifications

/// Ensures Nexar notification banners are shown even when the app is in the foreground.
///
/// Set as the delegate of `UNUserNotificationCenter.current()` once in `iOSApp.init()`.
/// Without this, iOS silently suppresses all local notifications while the app is active.
final class NexarNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    static let shared = NexarNotificationDelegate()

    // MARK: - Foreground presentation

    /// Show the banner, play the sound, and update the badge while Nexar is open.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    // MARK: - Tap handling

    /// Tapping the notification brings the app to the foreground (default OS behaviour).
    /// Override here to deep-link into specific screens when needed in the future.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
}
