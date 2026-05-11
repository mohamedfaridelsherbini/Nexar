---
name: permissions-health
description: "Implement the Nexar Permissions health roadmap slice. Use this when adding or updating camera, files/storage, or notifications permission status, platform-specific access checks, settings deep links, or the shared Settings/About UI that surfaces permission health in the Compose Multiplatform app."
---

# Permissions Health

Use this skill for the roadmap item `Permissions health`.

## Goal

Surface permission health in Settings/About with:
- current status for camera, files/storage, and notifications
- clear unavailable/denied/granted states
- deep links to the platform settings screen when recovery is possible

## Expected Output

Prefer this shape:
- shared domain or platform facade for permission status reads
- shared `SettingsViewModel` state for permission rows
- shared Settings UI rendering
- Android and iOS actual/platform implementations
- unit tests for shared mapping/state logic

Do not start in UI. Start from the platform seam and state model.

## Workflow

1. Inspect the current Settings pipeline:
   - `composeApp/src/commonMain/.../presentation/settings`
   - `composeApp/src/commonMain/.../domain/usecase/SettingsUseCases.kt`
   - `composeApp/src/commonMain/.../platform`

2. Add a shared abstraction for permission health.
   - Keep it small.
   - Prefer explicit enums/data classes over raw strings.
   - Include enough information for UI copy and CTA visibility.

3. Implement platform-specific readers.
   - Android: camera permission, notification permission, and storage/files capability relevant to the current export/import flow.
   - iOS: camera, photo/files capability if applicable, and notifications.
   - If a permission does not exist on one platform, expose a stable `not_required` or equivalent shared state instead of faking denial.

4. Add a platform action for opening app settings.
   - Keep this separate from status reading.
   - Shared code should request an action, not construct native URLs.

5. Thread the result into `SettingsViewModel`.
   - Avoid direct platform/global reads in the ViewModel constructor unless injected behind a seam.
   - Keep state deterministic for unit tests.

6. Update Settings UI.
   - One row per permission area.
   - Show current state and a recovery CTA only when meaningful.
   - Avoid platform-specific branching in composables when a shared state model can carry the difference.

7. Verify.
   - Add or update common tests first.
   - Write unit tests with `GIVEN / WHEN / THEN` naming and structure.
   - Compile Android and iOS targets.
   - If coverage is enforced, rerun Kover verification.

## Constraints

- Keep shared state unit-testable.
- Use `GIVEN / WHEN / THEN` for unit test names and arrange-act-assert flow.
- Prefer expect/actual or injected interfaces over static platform calls in shared logic.
- Do not bury permission logic directly in composables.
- Match existing project naming and package structure.

## Verification Commands

Run the smallest relevant set:

```bash
./gradlew :composeApp:testDebugUnitTest --tests "com.mohamedfaridelsherbini.nexar.presentation.settings.*"
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:koverVerify
```
