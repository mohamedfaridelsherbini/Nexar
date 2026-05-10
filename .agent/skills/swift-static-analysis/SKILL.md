---
name: swift-static-analysis
description: >-
  Sets up and runs Swift static analysis using SwiftLint, optional SwiftFormat,
  and Xcode Analyze (xcodebuild analyze). Use when the user asks for Swift code
  quality, SwiftLint, SwiftFormat, iOS lint CI, or analysis for iosApp.
---

# Swift static analysis

## Android Studio Gemini Agent Mode

Canonical layout per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)**. This folder: **`.agent/skills/swift-static-analysis/`**.

## Default stack

| Tool | Role |
|------|------|
| **SwiftLint** | De facto lint rules; `--strict` in CI; `.swiftlint.yml` in repo root or `iosApp/`. |
| **SwiftFormat** | Optional formatting; align with SwiftLint to avoid rule fights (one source of truth or explicit overlap rules). |
| **xcodebuild analyze** | Static analyzer without running the app; good PR gate. |

## Project layout (typical)

- Place `.swiftlint.yml` where the `iosApp` target sources live (often `iosApp/` or repo root).
- Document the Xcode scheme used for CI (e.g. `Nexar`).

## Local commands

```bash
swiftlint lint --strict
# If using SwiftFormat:
swiftformat . --lint
xcodebuild -scheme Nexar -destination 'platform=iOS Simulator,name=iPhone 16' analyze
```

Adjust **scheme** and **destination** to match `iosApp` settings.

## CI checklist

- [ ] `swiftlint lint --strict` on PRs touching `*.swift`.
- [ ] Optional: `xcodebuild … analyze` (slower; matrix or nightly if needed).
- [ ] Pin SwiftLint version (Mint, Homebrew pin, or prebuilt in CI image).

## Agent workflow

When asked to add or fix Swift analysis:

1. Read existing `iosApp` structure and any `.swiftlint.yml`.
2. Prefer incremental rule enablement over a huge default set that floods the PR.
3. Use `swiftlint lint --fix` only where safe; prefer explicit edits for risky autocorrect.
4. Match team naming: Nexar, `iosApp`, SwiftUI patterns already in the tree.

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/swift-static-analysis/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/swift-static-analysis`** | Symlink for Cursor Agent. |
| **`~/.cursor/skills/swift-static-analysis/`** | Optional global copy for Cursor. |

## Optional reference

See [reference.md](reference.md) for minimal `.swiftlint.yml` and CI notes.
