# Nexar Technical Context (Token Saver)

A concise technical snapshot to reduce discovery overhead for AI agents.

## 1. Core Stack
- **KMP**: Kotlin Multiplatform (Android + iOS).
- **UI**: Compose Multiplatform (shared) + SwiftUI (iOS host).
- **DI**: Koin (`AppModule.kt`).
- **Database**: Room v6 with FTS3 support (`AppDatabase.kt`).
- **Navigation**: Navigation3 (Compose).
- **Concurrency**: Coroutines + Flow.

## 2. Key Directories
- `shared/`: Non-UI logic, models, and interfaces.
- `composeApp/commonMain/`: Shared UI components and ViewModels.
- `composeApp/androidMain/` & `composeApp/iosMain/`: Platform-specific implementations (`expect`/`actual`).
- `iosApp/`: Native Swift host and high-fidelity SwiftUI mirrors.

## 3. Database Pattern (Room)
- **Entities**: Defined in `DocumentEntity.kt`.
- **FTS**: Use `@Fts3` (merged into main entity file) for searchable text.
- **DAOs**: `DocumentDao.kt` handles both main and FTS search via `MATCH`.

## 4. Platform Bridges (`expect`/`actual`)
- **Location**: `composeApp/src/.../platform/`
- **Services**: Haptics, Notifier, Clipboard, ShareBridge.
- **Initialization**: Context-bound services are initialized in `NexarApplication.onCreate` to avoid runtime crashes.

## 5. UI Logic (Shared)
- **ViewModels**: `DashboardViewModel.kt` uses `UiPreferences` data class to bundle UI state.
- **Components**: `DashboardComponents.kt` contains the library of shared widgets.

## 6. Coding Guardrails
- **No Logic in UI**: Keep processing in UseCases and state in ViewModels.
- **Mirroring**: If a UI change is visible in Compose, check `iosApp/` for the SwiftUI equivalent.
- **Linting**: Run `./gradlew detekt ktlintCheck` and `swiftlint lint iosApp`.
