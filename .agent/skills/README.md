# Agent skills (Android Studio standard)

This directory follows **Gemini in Android Studio — Agent Mode skills**, as documented here:

**[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)**

Per that guide, import skills by placing them under **`.skills/`** or **`.agent/skills/`** at the **project root**. This repo uses **`.agent/skills/`** next to existing `.agent/` metadata.

## Repo convention

Every new project skill must be added in all four AI locations:
- **`.agent/skills/`** as the canonical source
- **`.cursor/skills/`** as a symlink to the `.agent` skill
- **`.codex/skills/`** as a symlink to the `.agent` skill
- **`.claude/skills/`** as a symlink to the `.agent` skill

## Layout

Each capability is a folder with a **`SKILL.md`** file (YAML frontmatter + instructions), optionally `reference.md`.

| Folder | Purpose |
|--------|---------|
| `kotlin-static-analysis/` | detekt, ktlint, Android Lint, Gradle |
| `swift-static-analysis/` | SwiftLint, SwiftFormat, `xcodebuild analyze` |
| `kmp-ci-code-analysis/` | Combined CI for KMP + iOS |
| `nexar-roadmap-sync/` | Keep `plan.md` / `future-plan.md` aligned |
| `permissions-health/` | Implement shared Settings permission health + platform status/deep links |
| `roadmap-feature-pass/` | Take the next roadmap item from slice selection through verification |

## Cursor IDE

Cursor loads project skills from **`.cursor/skills/`**. Those entries are **symlinks** to this directory so one source of truth remains **`.agent/skills/`**.

## Codex

Codex reads matching repo-local symlinks from **`.codex/skills/`** so the canonical content remains in **`.agent/skills/`**.

## Claude

Claude reads matching repo-local symlinks from **`.claude/skills/`** so the canonical content remains in **`.agent/skills/`**.

## Personal (all projects)

Copy any skill folder to **`~/.cursor/skills/<name>/`** for Cursor-wide use. Do not use `~/.cursor/skills-cursor/` (reserved by Cursor).
