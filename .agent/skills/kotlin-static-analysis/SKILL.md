---
name: kotlin-static-analysis
description: >-
  Sets up and runs Kotlin static analysis for Gradle KMP/Android projects using
  detekt, ktlint (or Spotless), and Android Lint. Use when the user asks for
  Kotlin code quality, detekt, ktlint, Android Lint, SARIF, or CI checks for
  composeApp / commonMain / androidMain.
---

# Kotlin static analysis

## Android Studio Gemini Agent Mode

This skill follows the layout described in **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** (Agent Skills open standard). Canonical copy: **`.agent/skills/kotlin-static-analysis/`**.

## Android Studio (JetBrains) alignment

Committed project standards live here:

| Path | Purpose |
|------|---------|
| Repo root **`.editorconfig`** | Spacing, charset, Kotlin `ij_*` import prefs — Android Studio picks this up with **Editor > Code Style > Enable EditorConfig support**. |
| **`config/detekt/detekt.yml`** | detekt ruleset; Gradle should reference this path after the detekt plugin is applied. |
| **`config/README.md`** | Short setup steps for Android Studio + detekt. |

Do **not** commit `.idea/` for this repo (it is gitignored); use `.editorconfig` + `config/` instead of checked-in IDE XML.

## Default stack

| Tool | Role |
|------|------|
| **detekt** | Complexity, smells, coroutines, configurable rules; use `baseline.xml` when adopting on legacy code. |
| **ktlint** | Formatting + lightweight style (or **Spotless** + ktlint step). |
| **Android Lint** | `lint` on Android variants; resources, APIs, Compose-related checks. |
| **Compiler** | Prefer fixing warnings; consider `-Werror` only when noise is under control. |

## Gradle wiring (typical)

1. Add plugins in root or `composeApp/build.gradle.kts`: `io.gitlab.arturbosch.detekt`, `org.jlleitschuh.gradle.ktlint` (or Spotless).
2. Pin tool versions in `gradle/libs.versions.toml` when the project uses a version catalog.
3. Run locally:
   - `./gradlew detekt` (or `detektMain` depending on plugin)
   - `./gradlew ktlintCheck` (or `ktlintFormat` to fix)
   - `./gradlew :composeApp:lintDebug` (adjust module/variant)
4. For KMP: run detekt on `commonMain` + platform source sets; exclude generated dirs (`build/`, `generated/`).

## CI checklist

- [ ] Run detekt + ktlint (strict) on every PR touching `*.kt`.
- [ ] Fail on new detekt issues unless file is in baseline (shrink baseline over time).
- [ ] Optional: upload SARIF to GitHub Code Scanning (`detekt` SARIF report task).
- [ ] Cache Gradle between runs.

## Agent workflow

When asked to add or fix analysis:

1. Read existing `build.gradle.kts` / `libs.versions.toml` — match project conventions.
2. Add minimal config first (`detekt.yml` with a short ruleset); expand later.
3. Do not blanket-disable rules without a comment linking to a follow-up issue.
4. After edits, run the same Gradle tasks the user would run in CI and fix or document failures.

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/kotlin-static-analysis/`** | Canonical (Android Studio Gemini). See [parent README](../README.md). |
| **`.cursor/skills/kotlin-static-analysis`** | Symlink to this folder for Cursor Agent. |
| **`~/.cursor/skills/kotlin-static-analysis/`** | Optional copy for all Cursor projects (not `skills-cursor`). |

## Optional reference

For copy-paste snippets and version pinning examples, see [reference.md](reference.md).
