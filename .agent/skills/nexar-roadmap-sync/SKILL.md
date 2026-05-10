---
name: nexar-roadmap-sync
description: >-
  Keeps ROADMAP.md aligned when updating the Nexar product roadmap,
  priorities (P1/P2/P3), status, owner, effort, and target milestones.
  Use when the user edits the backlog tables in this repository.
---

# Nexar roadmap sync

## Android Studio Gemini Agent Mode

Canonical per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** — **`.agent/skills/nexar-roadmap-sync/`**.

## Source of truth

- **`ROADMAP.md`** — The unified source for strategic horizons and technical backlogs.

## Rules

1. When adding, removing, or re-prioritizing a feature: update the tables in **`ROADMAP.md`**.
2. When an item ships: set **Status** to `done` in the Prioritized Backlog and append a bullet under **Already in place** (Section 5).
3. Keep **Priority** (`P1` / `P2` / `P3`) aligned with the product goals.
4. Use `TBD` for Owner until assigned; replace with a person or team name when known.

## Effort legend

- `S` — small (under ~3 days)
- `M` — medium (~3–10 days)
- `L` — large (multi-week / multi-sprint)

## Agent checklist

- [ ] Tables render as valid Markdown (pipe alignment optional).
- [ ] Section 5 (Already in place) reflects the latest technical achievements.

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/nexar-roadmap-sync/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/nexar-roadmap-sync`** | Symlink for Cursor Agent. |
| **`~/.cursor/skills/nexar-roadmap-sync/`** | Optional global copy for Cursor. |
