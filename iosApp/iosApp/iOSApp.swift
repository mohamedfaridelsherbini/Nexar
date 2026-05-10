import SwiftUI
import UserNotifications

@main
struct IOSApp: App {
    @AppStorage("onboarding_complete") private var onboardingComplete = false
    @AppStorage("app_theme") private var appTheme = "System"

    init() {
        // Register the delegate so notifications display while the app is foregrounded.
        UNUserNotificationCenter.current().delegate = NexarNotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if onboardingComplete {
                    ContentView()
                } else {
                    OnboardingView {
                        onboardingComplete = true
                    }
                }
            }
            .preferredColorScheme(colorScheme)
        }
    }

    private var colorScheme: ColorScheme? {
        switch appTheme {
        case "Light": return .light
        case "Dark":  return .dark
        default:      return nil
        }
    }
}
