---
name: roadmap-feature-pass
description: "Implement the next Nexar roadmap slice with the project's expected delivery loop: read ROADMAP.md, choose the next actionable item, add the smallest end-to-end slice, update tests and coverage, and verify Android and iOS compilation before reporting status."
---

# Roadmap feature pass

## Android Studio Gemini Agent Mode

Canonical per **[Extend Agent Mode with skills](https://developer.android.com/studio/gemini/skills)** — **`.agent/skills/roadmap-feature-pass/`**.

## Workflow

1. Read `ROADMAP.md`.
2. Pick the next item that is both:
   - highest priority among `todo`
   - small enough for one concrete slice
3. State the slice before editing.
4. Implement end-to-end, not just one layer.
   - domain/state
   - UI wiring if needed
   - tests for shared logic using `GIVEN / WHEN / THEN`
5. Update `ROADMAP.md` only if the status meaningfully changed.
6. Verify:
   - targeted tests
   - Android compile
   - iOS compile
   - coverage verification if shared logic changed

## Selection rule

Prefer:
- `P1` over `P2`/`P3`
- `todo` over broadening an already `in_progress` item
- the smallest user-visible slice that reduces product risk

For Nexar right now, `Permissions health` is the default next step unless the user redirects.

## Reporting

When finished, report:
- what slice was implemented
- which files carry the main behavior
- what verification passed
- what the next smallest follow-up slice is

## Where this skill lives

| Location | Role |
|----------|------|
| **`.agent/skills/roadmap-feature-pass/`** | Canonical (Android Studio Gemini). |
| **`.cursor/skills/roadmap-feature-pass`** | Symlink for Cursor Agent. |
| **`.codex/skills/roadmap-feature-pass/`** | Codex-local copy. |
