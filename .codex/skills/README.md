# Codex skills

This directory holds the repo-local **Codex** symlinks to project skills.

## Repo convention

Every project skill should exist in all four AI locations:

- **`.agent/skills/`**: canonical source for Android Studio Gemini Agent Mode
- **`.cursor/skills/`**: symlinks to `.agent/skills/`
- **`.codex/skills/`**: Codex-local symlinks to `.agent/skills/`
- **`.claude/skills/`**: Claude-local symlinks to `.agent/skills/`

When adding a new skill:

1. Create the canonical folder in **`.agent/skills/<name>/`**
2. Add the matching symlink in **`.cursor/skills/<name>`**
3. Add the matching symlink in **`.codex/skills/<name>`**
4. Add the matching symlink in **`.claude/skills/<name>`**

Keep the skill name and core instructions aligned across all four locations.
