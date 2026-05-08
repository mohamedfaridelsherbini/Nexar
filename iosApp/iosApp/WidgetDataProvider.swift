import Foundation

/// Writes shared data for the NexarWidget WidgetKit extension via App Group UserDefaults.
/// The widget target reads these values with the same suite name.
///
/// To enable App Group sharing:
///   1. In Xcode → target → Signing & Capabilities, add "App Groups" for both the main app
///      and the NexarWidget extension, using the same identifier (e.g. "group.com.mohamedfaridelsherbini.nexar").
///   2. Replace the suite name below with that identifier.
enum WidgetDataProvider {
    private static let suiteName = "group.com.mohamedfaridelsherbini.nexar"
    private static let keyUnexported = "nexar.widget.unexportedCount"
    private static let keyLastScan   = "nexar.widget.lastScanName"

    static func update(unexportedCount: Int, lastScanName: String) {
        let defaults = UserDefaults(suiteName: suiteName) ?? .standard
        defaults.set(unexportedCount, forKey: keyUnexported)
        defaults.set(lastScanName, forKey: keyLastScan)
        // Ask WidgetKit to reload all timelines
        if #available(iOS 14.0, *) {
            reloadWidgetTimelines()
        }
    }

    static func readUnexportedCount() -> Int {
        let defaults = UserDefaults(suiteName: suiteName) ?? .standard
        return defaults.integer(forKey: keyUnexported)
    }

    static func readLastScanName() -> String {
        let defaults = UserDefaults(suiteName: suiteName) ?? .standard
        return defaults.string(forKey: keyLastScan) ?? "No scans yet"
    }
}

@available(iOS 14.0, *)
private func reloadWidgetTimelines() {
    // WidgetKit is in a separate framework to avoid linking it in non-extension targets.
    // We use NSClassFromString to call reloadAllTimelines() without a hard link.
    if let widgetCenterClass = NSClassFromString("WidgetCenter") as? NSObject.Type,
       let center = widgetCenterClass.value(forKey: "shared") as? NSObject {
        center.perform(NSSelectorFromString("reloadAllTimelines"))
    }
}
