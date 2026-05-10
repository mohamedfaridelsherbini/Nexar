import SwiftUI
import UserNotifications

@main
struct IOSApp: App {
    @AppStorage("onboarding_complete") private var onboardingComplete = false

    init() {
        // Register the delegate so notifications display while the app is foregrounded.
        UNUserNotificationCenter.current().delegate = NexarNotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            if onboardingComplete {
                ContentView()
            } else {
                OnboardingView {
                    onboardingComplete = true
                }
            }
        }
    }
}
