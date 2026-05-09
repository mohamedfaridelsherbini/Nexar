# Nexar — future plan

Forward-looking backlog and horizons. For the **full tabular roadmap** (all workstreams + “already shipped”), see [`plan.md`](plan.md).

---

## How to use this file

- **`plan.md`** — detailed items, tables, and reference to what exists today.  
- **`future-plan.md`** — time horizons, themes, and outcomes so planning stays lightweight.

Update both when you ship or reprioritize.

---

## Near term (next few releases)

**Goal:** tighten the core loop (scan → organize → export) and user control.

| Theme | Outcomes |
|--------|-----------|
| **Settings** | Theme, defaults, notification toggles, about/version. |
| **Search UX** | Highlights, recent queries, chips — on top of FTS. |
| **Notifications** | Stable IDs / grouping (Android), categories & actions (iOS), user toggles. |
| **Quality** | More tests, CI lint + unit tests, privacy-safe logging. |

---

## Mid term

**Goal:** power-user workflows and system fit.

| Theme | Outcomes |
|--------|-----------|
| **Documents** | Merge/split PDFs, duplicate resolution UX (“not a duplicate”, link to original). |
| **Deep links & shortcuts** | Open doc / scanner / folder from links, widgets, and shortcuts. |
| **Import** | Bring files into Nexar from Files / share targets with consistent processing. |
| **Export presets** | Multiple destinations, naming patterns, optional auto-export. |

---

## Long term

**Goal:** backup, sync, and enterprise-adjacent needs (only if product commits).

| Theme | Outcomes |
|--------|-----------|
| **Cloud** | iCloud / Drive or provider model; conflicts; encryption & privacy stance. |
| **Rules & templates** | User-defined naming/categorization rules. |
| **Security** | App lock, stricter data boundaries, audit-friendly export. |

---

## Missing features (aligned with `plan.md`)

Same **P1 / P2 / P3** items as section 7 in [`plan.md`](plan.md). Horizon placement below; copy Status / Owner / Effort / Target from `plan.md` when executing.

| Priority | Feature | Horizon | Status | Owner | Effort | Target |
|:---:|---|---|:---:|:---:|:---:|:---:|
| **P1** | Trash / restore | Near | todo | TBD | M | near |
| **P1** | Bulk actions | Near | todo | TBD | M | near |
| **P1** | Permissions health | Near | todo | TBD | S | near |
| **P1** | Backup & recovery UX | Near | todo | TBD | M | near |
| **P2** | OCR language settings | Mid | todo | TBD | M | mid |
| **P2** | Sync conflict resolution UX | Mid | todo | TBD | L | mid |
| **P2** | Storage cleanup center | Mid | todo | TBD | M | mid |
| **P2** | Offline queue visibility | Mid | todo | TBD | M | mid |
| **P3** | Document security controls | Long | todo | TBD | L | long |
| **P3** | Release readiness | Mid | todo | TBD | M | mid |

---

## Success metrics (examples)

Track only what you will act on; keep PII out of analytics.

- Time from scan to first successful export.  
- % of sessions with at least one export.  
- Search usage and zero-result rate.  
- Crash-free sessions and notification opt-in / opt-out (if toggles exist).

---

## Dependencies & risks

- **Cloud sync** — highest uncertainty (provider choice, conflict UX, cost).  
- **Share extensions** — platform review and sandbox constraints (especially iOS).  
- **Performance** — large libraries and PDF memory before adding heavy sync.

---

**Maintenance:** When an item ships, set Status to `done` in **both** this table and [`plan.md`](plan.md) section 7, and add a line under **Already in place** in `plan.md`.

---

*Last aligned with `plan.md` section 7 — update both files together when priorities shift.*
