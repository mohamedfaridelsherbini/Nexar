# AGENT.md

Guidance for AI coding agents working in this repository. Read this first, then `ROADMAP.md`, `DESIGN.md`, and `Nexar.pen` before touching UI.

## Project snapshot

- Kotlin Multiplatform document scanner targeting Android and iOS.
- Gradle modules (`settings.gradle.kts`):
  - `:composeApp` shared Compose UI plus Android entry point.
  - `:shared` cross-target non-UI code.
- iOS entry point: `iosApp/iosApp.xcodeproj` (SwiftUI host).
- Single source of design truth: [`Nexar.pen`](Nexar.pen) (token + screen spec) and [`DESIGN.md`](DESIGN.md) (narrative spec).

## Repo map

- `composeApp/src/commonMain/kotlin/com/mohamedfaridelsherbini/nexar/`
  - `ui/DashboardScreen.kt` main shared screen.
  - `ui/components/DashboardComponents.kt` reusable building blocks.
  - `ui/theme/Color.kt`, `ui/theme/Type.kt`, `ui/theme/Scheme.kt` token mirrors.
- `composeApp/src/androidMain/.../ui/theme/Theme.kt` Android `actual` theme.
- `composeApp/src/iosMain/.../ui/theme/Theme.ios.kt` iOS `actual` theme.
- `shared/src/` cross-target non-UI code.
- `iosApp/iosApp/ContentView.swift` and `iosApp/iosApp/NexarDesign.swift` SwiftUI surface.

## Build and run

- Android debug build: `./gradlew :composeApp:assembleDebug`
- Android install on device: `./gradlew :composeApp:installDebug`
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the IDE run config.
- Tests: only `composeApp/src/androidUnitTest` scaffolding exists; add tests there when introducing logic worth covering.

## Design system rules (must follow)

- Tokens live in `Nexar.pen` under the `variables` block and are mirrored in `ui/theme/Color.kt` and `ui/theme/Type.kt`. Keep these three in lockstep.
- Always use semantic tokens, never raw hex. In the pen file use `$accent-primary`; in Compose use `NexarAccentPrimary` (or the dark twin).
- Light and dark are parallel sets: `Nexar*` and `NexarDark*`. When you add a color, add both.
- Approved scales (snap to these):
  - Spacing: `4 / 8 / 12 / 16 / 20 / 24 / 28`
  - Corner radius: `4 / 8 / 12 / 16 / 24 / 999`
  - Type ramp (sp): `11, 12, 14, 16, 18, 22, 28, 34, 48`
- Accent `#0EA5A4` is reserved for the scan/export action path. Do not use it as general decoration.
- Only one primary action per screen; secondary actions stay quiet.

## Coding conventions

- Package root: `com.mohamedfaridelsherbini.nexar`.
- Compose-first UI. Use `expect`/`actual` for platform theme (one `Theme.kt` per source set).
- Keep imports at the top of the file. No inline imports.
- Do not add narrating comments. Only comment non-obvious intent, trade-offs, or constraints.
- Prefer editing existing files over creating new ones; do not introduce new top-level modules without reason.

## Agent workflow guidance

- Before any UI change, skim `DESIGN.md` and the relevant frame in `Nexar.pen`.
- When behavior is user-visible, mirror Compose changes in the iOS SwiftUI surface (`ContentView.swift`, `NexarDesign.swift`) so platforms stay in sync.
- Do not commit unless the user explicitly asks.
- Do not modify `local.properties`, files under `gradle/`, or `iosApp/iosApp.xcodeproj` unless the task requires it.
- Ignore `build/` directories when gathering context.

## Known gaps (open follow-ups)

- `Nexar.pen` declares `dark-error` and `dark-surface-elevated` but never uses them.
- Several hardcoded hex colors remain in `Nexar.pen` (notably `#FFFFFF`, `#0F172A`, `#F8FAFC`, `#CBD5E1`, accent and warning tints) and should be promoted to tokens.
- `ui/theme/Type.kt` still uses `FontFamily.Default`; the design specifies Inter for `font-body`, `font-caption`, `font-data`, and `font-heading`.
