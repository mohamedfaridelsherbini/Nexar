# Nexar — product & engineering roadmap

A unified source of truth for Nexar's strategic horizons and tactical engineering backlog.

---

## 1. Strategic Horizons

| Horizon | Goal | Outcomes |
|:---:|---|---|
| **Near term** | Tighten the core loop | Settings, Search UI refinements, stable notifications, and bulk actions. |
| **Mid term** | Power-user workflows | PDF merge/split, deep links, system shortcuts, and auto-export presets. |
| **Long term** | Ecosystem & Security | Cloud sync (iCloud/Drive), user rules/templates, and app-lock security. |

---

## 2. Prioritized Backlog

| Priority | Feature | Notes | Status | Owner | Effort | Target |
|:---:|---|---|:---:|:---:|:---:|:---:|
| **P1** | iOS interaction stability | Eliminate remaining scan-button shadow/highlight rendering glitches across SwiftUI and Compose interop surfaces. | in_progress | TBD | S | near |
| **P1** | Trash / restore | Soft-delete with 30-day restore window. | todo | TBD | M | near |
| **P1** | Bulk actions | Multi-select export, delete, tag, and share. | todo | TBD | M | near |
| **P1** | Permissions health | Camera/files/notifications status + deep links to settings. | todo | TBD | S | near |
| **P1** | Backup & recovery | Explicit backup/restore flow before full cloud sync. | todo | TBD | M | near |
| **P2** | OCR settings | User-selectable languages and fallback logic. | todo | TBD | M | mid |
| **P2** | Sync conflict UI | Resolution flows for keep-local vs keep-remote. | todo | TBD | L | mid |
| **P2** | Storage cleanup | Large file identification and duplicate cleanup helpers. | todo | TBD | M | mid |
| **P3** | Watermarking | Redact before share or optional watermark overlay. | todo | TBD | L | long |
| **P3** | Release readiness | In-app feedback, changelog, and feature flags. | todo | TBD | M | mid |

**Effort:** `S` (<3 days), `M` (3-10 days), `L` (>2 weeks).

---

## 3. Tactical Breakdowns

### Product & UX
- **Settings / About**: Theme selection, notification toggles, and app version are implemented; next step is permissions/status surfacing.
- **Search UX**: Highlight matches in list, recent query chips, empty-state guidance.
- **Accessibility**: VoiceOver/TalkBack pass, dynamic type, and contrast audit.
- **iOS Visual Stability**: Finish validation of scan CTA press/shadow behavior across SwiftUI and Compose-backed screens.

### Intelligence & Documents
- **Merge / Split**: Combine multiple scans into one PDF; split one doc into many.
- **Duplicate UX**: Surface "similar to: [name]" with navigation to the original.
- **Auto-Rename**: User-defined templates or category-based naming rules.

### Storage & System
- **Cloud Sync**: iCloud Drive / Google Drive integration; conflict policies.
- **Import**: Ingest PDFs/images from system Files into the Nexar pipeline.
- **Widgets**: Extend beyond unexported count; add "Last scan" quick-access.

---

## 4. Success Metrics & Risks

### Metrics
- Time from scan to first successful export.
- % of sessions with at least one export.
- Search usage and zero-result rate.

### Risks
- **Cloud sync complexity**: High uncertainty around provider sandboxes and conflict UX.
- **Performance**: High memory usage for multi-page PDFs on low-RAM devices.

---

## 5. Already in place (Done)

- **Quality**: Full static analysis pipeline implemented (detekt, ktlint, SwiftLint).
- **Core Engine**: OCR pipeline (ML Kit / Vision), auto-categorization, FTS search.
- **UI Architecture**: KMP shared UI with Android Compose & iOS SwiftUI mirrors.
- **Database**: Room (v6) with FTS3 support and destructive migration safety.
- **Notifications**: Reminders for pending exports and duplicate alerts.
- **Settings**: Theme selection, duplicate/export reminder toggles, and platform-backed app version display.
- **UI Polish**: Animations, empty states, and accessibility refinements on dashboard.
- **iOS UI Stability**: Reworked top-bar button press states and local scan CTA shadow/highlight ownership to reduce detached overlay rendering.
- **Widget**: Android Glance widget for unexported document tracking.
