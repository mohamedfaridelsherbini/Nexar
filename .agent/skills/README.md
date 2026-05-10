# Agent skills (Android Studio standard)

This directory follows **Gemini in Android Studio — Agent Mode skills**, as documented here:

**[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)**

Per that guide, import skills by placing them under **`.skills/`** or **`.agent/skills/`** at the **project root**. This repo uses **`.agent/skills/`** next to existing `.agent/` metadata.

## Layout

Each capability is a folder with a **`SKILL.md`** file (YAML frontmatter + instructions), optionally `reference.md`.

| Folder | Purpose |
|--------|---------|
| `kotlin-static-analysis/` | detekt, ktlint, Android Lint, Gradle |
| `swift-static-analysis/` | SwiftLint, SwiftFormat, `xcodebuild analyze` |
| `kmp-ci-code-analysis/` | Combined CI for KMP + iOS |
| `nexar-roadmap-sync/` | Keep `plan.md` / `future-plan.md` aligned |

## Cursor IDE

Cursor loads project skills from **`.cursor/skills/`**. Those entries are **symlinks** to this directory so one source of truth remains **`.agent/skills/`**.

## Personal (all projects)

Copy any skill folder to **`~/.cursor/skills/<name>/`** for Cursor-wide use. Do not use `~/.cursor/skills-cursor/` (reserved by Cursor).
