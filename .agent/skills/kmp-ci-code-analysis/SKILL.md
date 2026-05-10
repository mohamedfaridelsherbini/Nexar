---
name: kmp-ci-code-analysis
description: >-
  Orchestrates combined CI checks for Kotlin Multiplatform + iOS repos: Gradle
  tests and detekt/ktlint/lint plus SwiftLint (and optional xcodebuild analyze).
  Use when the user asks for GitHub Actions, PR checks, or one pipeline for
  Nexar-style composeApp + iosApp.
---

# KMP + iOS CI code analysis

## Android Studio Gemini Agent Mode

Canonical per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** — this folder: **`.agent/skills/kmp-ci-code-analysis/`**.

## Goal

One PR should prove:

- Kotlin module(s) compile and unit tests pass.
- Kotlin static analysis passes (detekt + ktlint + Android `lint` as applicable).
- SwiftLint passes for `iosApp` (strict).
- Optional: Xcode **Analyze** on a schedule or main-only if runtime cost is high.

## Suggested job split

| Job | When | Notes |
|-----|------|------|
| `kotlin` | Every PR | `./gradlew detekt ktlintCheck test` + `:composeApp:lintDebug` (names vary). |
| `swiftlint` | PRs touching `*.swift` | `paths: iosApp/**/*.swift` filter in GitHub Actions. |
| `analyze` | Nightly or main | `xcodebuild analyze` — slow; cache DerivedData when possible. |

## Agent workflow

1. Inspect repo: `composeApp/`, `iosApp/`, existing `.github/workflows/`.
2. Reuse JDK and Xcode versions already implied by the project.
3. Add caching: Gradle (`~/.gradle`), SPM/DerivedData if used.
4. Do not add secrets unless the user explicitly needs signing for tests.
5. Keep workflow files small; extract composite action only if duplication hurts.

## Nexar-specific hints

- Android entry is often `composeApp`; Koin + Room tests live under `commonTest` / `androidTest` as applicable.
- iOS sources under `iosApp/iosApp/`; confirm scheme name in Xcode before hardcoding in YAML.

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/kmp-ci-code-analysis/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/kmp-ci-code-analysis`** | Symlink for Cursor Agent. |
| **`~/.cursor/skills/kmp-ci-code-analysis/`** | Optional global copy for Cursor. |

## Reference

Split details live in sibling skills: [kotlin-static-analysis](../kotlin-static-analysis/SKILL.md), [swift-static-analysis](../swift-static-analysis/SKILL.md).
