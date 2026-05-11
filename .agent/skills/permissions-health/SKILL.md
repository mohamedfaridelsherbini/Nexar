---
name: permissions-health
description: >-
  Implement the Nexar Permissions health roadmap slice. Use this when adding or
  updating camera, files/storage, or notifications permission status,
  platform-specific access checks, settings deep links, or the shared
  Settings/About UI that surfaces permission health in the Compose
  Multiplatform app.
---

# Permissions health

## Android Studio Gemini Agent Mode

Canonical per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** — **`.agent/skills/permissions-health/`**.

## Goal

Surface permission health in Settings/About with:
- current status for camera, files/storage, and notifications
- clear unavailable/denied/granted states
- deep links to the platform settings screen when recovery is possible

## Expected output

Prefer this shape:
- shared domain or platform facade for permission status reads
- shared permission status model with explicit enums/data classes
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
   - Prefer one model such as:
     - `PermissionArea`: camera, notifications, files
     - `PermissionState`: granted, denied, unavailable, not_required
     - `PermissionAction`: none, open_settings
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

## Platform checklist

### Android

- Camera: `android.Manifest.permission.CAMERA`
- Notifications: `android.Manifest.permission.POST_NOTIFICATIONS` where applicable by SDK level
- Files/storage:
  - do not invent a legacy storage permission if the app flow uses SAF
  - expose capability based on the current export/import mechanism
- Settings deep link: app-details settings intent, not a custom screen-specific URL

### iOS

- Camera: `AVCaptureDevice` or equivalent camera authorization status
- Notifications: `UNUserNotificationCenter` authorization status
- Files/storage:
  - if the app uses document pickers or share sheets without a dedicated permission, prefer `not_required`
- Settings deep link: app settings URL only from the platform action layer

## Test pattern

Write shared tests before UI tests.

Preferred test names:
- `GIVEN denied camera permission WHEN settings state is built THEN camera row shows denied with open settings action`
- `GIVEN notification permission is not required WHEN platform status is mapped THEN row shows not required`
- `GIVEN user taps open settings WHEN row action is triggered THEN platform bridge is called once`

Preferred structure:
1. `GIVEN`: set up fake permission provider and fake settings action bridge
2. `WHEN`: build or refresh `SettingsViewModel` state, or trigger the row action
3. `THEN`: assert the shared row state and action behavior

## Slice order

Implement in this order unless the user directs otherwise:
1. shared status model
2. platform readers
3. `SettingsViewModel` wiring
4. Settings UI rows
5. settings deep link action
6. tests and verification

## Constraints

- Keep shared state unit-testable.
- Use `GIVEN / WHEN / THEN` for unit test names and arrange-act-assert flow.
- Prefer expect/actual or injected interfaces over static platform calls in shared logic.
- Do not bury permission logic directly in composables.
- Match existing project naming and package structure.

## Verification commands

```bash
./gradlew :composeApp:testDebugUnitTest --tests "com.mohamedfaridelsherbini.nexar.presentation.settings.*"
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:compileKotlinIosSimulatorArm64
./gradlew :composeApp:koverVerify
```

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/permissions-health/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/permissions-health`** | Symlink for Cursor Agent. |
| **`.codex/skills/permissions-health`** | Symlink for Codex. |
| **`.claude/skills/permissions-health`** | Symlink for Claude. |
