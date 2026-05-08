import SwiftUI

// MARK: - Model

private struct OnboardingPage: Identifiable {
    let id: Int
    let systemImage: String
    let title: String
    let body: String
    let actionLabel: String
}

private let onboardingPages: [OnboardingPage] = [
    .init(id: 0,
          systemImage: "sparkles",
          title: "Welcome to Nexar",
          body: "Your intelligent document scanner that automatically reads, names, and organizes every document you capture.",
          actionLabel: "Get started"),
    .init(id: 1,
          systemImage: "camera.viewfinder",
          title: "Scan & Read",
          body: "Point your camera at any receipt, invoice, contract, ID, or medical record. Nexar's OCR engine reads the text and suggests a smart name instantly.",
          actionLabel: "Next"),
    .init(id: 2,
          systemImage: "rectangle.3.group",
          title: "Auto-Organized",
          body: "Every scan is automatically classified, tagged, and enriched with extracted amounts and dates. No manual sorting — ever.",
          actionLabel: "Next"),
    .init(id: 3,
          systemImage: "folder.badge.plus",
          title: "Export Anywhere",
          body: "Save your documents to any folder — iCloud Drive, Files, or external storage. Set an export folder once, then tap to export.",
          actionLabel: "Done")
]

// MARK: - Root view

/// First-launch walkthrough presented over `ContentView` when `onboarding_complete`
/// is not set in `UserDefaults`. Calls `onComplete` on "Done" or "Skip".
struct OnboardingView: View {
    let onComplete: () -> Void

    @State private var currentPage = 0

    private var isLast: Bool { currentPage == onboardingPages.count - 1 }
    private var page: OnboardingPage { onboardingPages[currentPage] }

    var body: some View {
        ZStack(alignment: .top) {
            NexarColor.surfacePrimary.ignoresSafeArea()

            // Skip button
            if !isLast {
                HStack {
                    Spacer()
                    Button("Skip", action: onComplete)
                        .font(.subheadline)
                        .foregroundColor(NexarColor.foregroundMuted)
                        .padding(.top, 16)
                        .padding(.trailing, 24)
                }
            }

            // Page content with swipe / animated transition
            TabView(selection: $currentPage) {
                ForEach(onboardingPages) { p in
                    PageContent(page: p)
                        .tag(p.id)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .animation(.easeInOut(duration: 0.35), value: currentPage)

            // Bottom bar
            VStack(spacing: 28) {
                Spacer()

                DotIndicator(total: onboardingPages.count, current: currentPage)

                Button {
                    withAnimation(.easeInOut(duration: 0.35)) {
                        if isLast { onComplete() }
                        else { currentPage += 1 }
                    }
                } label: {
                    Text(page.actionLabel)
                        .font(.system(size: 17, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(NexarColor.accentPrimary)
                        .foregroundColor(.white)
                        .clipShape(Capsule())
                }
                .padding(.horizontal, 28)
                .padding(.bottom, 48)
            }
        }
    }
}

// MARK: - Page content

private struct PageContent: View {
    let page: OnboardingPage

    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 80)

            // Icon bubble
            ZStack {
                Circle()
                    .fill(NexarColor.accentPrimary.opacity(0.10))
                    .frame(width: 120, height: 120)
                Image(systemName: page.systemImage)
                    .font(.system(size: 48, weight: .light))
                    .foregroundColor(NexarColor.accentPrimary)
            }

            Spacer(minLength: 36)

            Text(page.title)
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(NexarColor.foregroundPrimary)
                .multilineTextAlignment(.center)

            Spacer(minLength: 16)

            Text(page.body)
                .font(.body)
                .foregroundColor(NexarColor.foregroundSecondary)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.horizontal, 36)

            Spacer(minLength: 160)
        }
    }
}

// MARK: - Dot indicator

private struct DotIndicator: View {
    let total: Int
    let current: Int

    var body: some View {
        HStack(spacing: 8) {
            ForEach(0..<total, id: \.self) { idx in
                Capsule()
                    .fill(idx == current ? NexarColor.accentPrimary : NexarColor.borderSubtle)
                    .frame(width: idx == current ? 20 : 8, height: 8)
                    .animation(.spring(response: 0.3), value: current)
            }
        }
    }
}
