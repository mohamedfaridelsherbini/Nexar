---
name: nexar-roadmap-sync
description: >-
  Keeps plan.md and future-plan.md aligned when updating the Nexar product
  roadmap, priorities (P1/P2/P3), status, owner, effort, and target milestones.
  Use when the user edits roadmap files, future-plan.md, plan.md, or backlog
  tables in this repository.
---

# Nexar roadmap sync

## Android Studio Gemini Agent Mode

Canonical per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** — **`.agent/skills/nexar-roadmap-sync/`**.

## Source of truth

- **`plan.md`** — full tables, section 7 prioritized backlog with Status / Owner / Effort / Target.
- **`future-plan.md`** — horizons (near / mid / long) + the same priority rows; must mirror section 7.

## Rules

1. When adding, removing, or re-prioritizing a feature: update **both** files in the same change.
2. When an item ships: set **Status** to `done` in both tables; append a one-line bullet under **Already in place** in `plan.md` only (section 9).
3. Keep **Priority** (`P1` / `P2` / `P3`) identical across files; **Horizon** in `future-plan.md` should match **Target** (`near` / `mid` / `long`).
4. Use `TBD` for Owner until assigned; replace with a person or team name when known.

## Effort legend (plan.md)

- `S` — small (under ~3 days)
- `M` — medium (~3–10 days)
- `L` — large (multi-week / multi-sprint)

## Agent checklist

- [ ] Tables render as valid Markdown (pipe alignment optional).
- [ ] Cross-links at top of each file still point to the sibling file.
- [ ] Suggested implementation order in `plan.md` reflects current priorities.

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/nexar-roadmap-sync/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/nexar-roadmap-sync`** | Symlink for Cursor Agent. |
| **`~/.cursor/skills/nexar-roadmap-sync/`** | Optional global copy for Cursor. |
