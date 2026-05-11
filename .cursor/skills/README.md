# Cursor Agent skills (symlinks)

Canonical skill folders live under **`.agent/skills/`**, per Android Studio **Gemini Agent Mode**:

**[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)**

Each name here is a **symlink** to the matching folder under `.agent/skills/` so Cursor and Android Studio share one source of truth.

Edit files in **`.agent/skills/<name>/`**, not duplicate copies under `.cursor/skills/`.

Repo convention:
- add the canonical skill in **`.agent/skills/`**
- add the Cursor symlink here
- add the Codex copy in **`.codex/skills/`**
- add the Claude copy in **`.claude/skills/`**

Current shared skills include:
- `kmp-ci-code-analysis`
- `kotlin-static-analysis`
- `nexar-roadmap-sync`
- `permissions-health`
- `roadmap-feature-pass`
- `swift-static-analysis`
