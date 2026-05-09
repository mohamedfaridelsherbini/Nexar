# Nexar — product & engineering roadmap

Living backlog of enhancements across Android (Compose Multiplatform), iOS (SwiftUI), and shared KMP layers. Prioritize based on user research and release goals.

**Horizon view (near / mid / long term):** see [`future-plan.md`](future-plan.md).

---

## 1. Product & UX

| Item | Notes |
|------|--------|
| **Settings / About** | Theme (follow system / light / dark), default sort/filter, notification toggles, app version, privacy link, optional “clear recent searches” if that feature exists. |
| **Onboarding v2** | Optional: camera permission copy, dedicated “choose export folder” step, clearer value props per screen. |
| **Search UX** | Highlight matches in list/detail, recent searches, suggestion chips, stronger empty-search guidance. |
| **Empty & error states** | Richer empty illustrations, explicit retry, offline-oriented copy where relevant. |
| **Accessibility** | Full VoiceOver / TalkBack pass; dynamic type; contrast and focus order audit on every screen. |

---

## 2. Documents & intelligence

| Item | Notes |
|------|--------|
| **Merge / split PDFs** | Combine multiple scans into one multi-page PDF; optional split of one doc into several. |
| **Manual duplicate resolution** | “Not a duplicate” / link to canonical document; tune similarity thresholds in UI copy. |
| **Duplicate UX** | Surface “similar to: [name]” with navigation to the original. |
| **Templates / rules** | User-defined rename patterns or default category rules (advanced). |

---

## 3. Storage & sync

| Item | Notes |
|------|--------|
| **Cloud backup / sync** | iCloud Drive, Google Drive, or document-provider model; conflict policy; privacy & encryption story. |
| **Export presets** | Multiple destinations, filename patterns, optional auto-export after scan. |
| **Import** | Ingest PDFs/images from Files into Nexar through the same OCR / classify pipeline where applicable. |

---

## 4. Notifications

| Item | Notes |
|------|--------|
| **Android** | Stable notification IDs (update vs. spam); grouped/summary notifications; `BigTextStyle` for long bodies; optional one-off WorkManager run after `POST_NOTIFICATIONS` is granted. |
| **iOS** | `UNNotificationCategory` + action buttons; cancel delivered export reminder when pending count is zero; document notification permission in Settings deep link. |
| **Cross-platform** | User toggles: export reminders on/off, duplicate alerts on/off; future: quiet hours. |

---

## 5. Deep links & system integration

| Item | Notes |
|------|--------|
| **Deep links** | Open document by id, open export location, open scanner from URL or widget tap. |
| **Shortcuts & widgets** | Android App Shortcuts; iOS App Intents; extend widget beyond unexported count / last scan name. |
| **Share / receive** | “Save to Nexar” share extension / Android share target. |

---

## 6. Technical & quality

| Item | Notes |
|------|--------|
| **Tests** | More ViewModel flows; repository / DB integration tests; Swift unit tests for `ProcessDocumentUseCase` and notification scheduling. |
| **CI** | Lint, KMP unit tests, optional UI smoke on both platforms. |
| **Observability** | Structured logging; privacy-safe analytics (scan completed, export success/fail, errors). |
| **Performance** | Large libraries, FTS tuning, thumbnails, PDF memory on low-RAM devices. |
| **Security** | Audit paths and logs for PII; avoid sensitive snippets in notifications; optional app lock / biometrics. |

---

## 7. Missing features & prioritized backlog

Detailed gaps beyond sections 1–6. Update **Status** when shipping (`todo` → `in progress` → `done`). Use **Owner** for whoever owns delivery; **Target** is a milestone placeholder (`near`, `mid`, `long`, or quarter).

| Priority | Feature | Notes | Status | Owner | Effort | Target |
|:---:|---|---|---|:---:|:---:|:---:|
| **P1** | **Trash / restore** | Soft-delete with restore window (e.g. 30 days); optional permanent purge. | todo | TBD | M | near |
| **P1** | **Bulk actions** | Multi-select export, delete, tag/category, share. | todo | TBD | M | near |
| **P1** | **Permissions health** | Camera / files / notifications status + deep links to OS settings. | todo | TBD | S | near |
| **P1** | **Backup & recovery UX** | Explicit backup/restore story before full cloud sync. | todo | TBD | M | near |
| **P2** | **OCR language settings** | User-selectable languages / fallback for Vision & ML Kit. | todo | TBD | M | mid |
| **P2** | **Sync conflict resolution UX** | Keep-local / keep-remote / merge flows when sync lands. | todo | TBD | L | mid |
| **P2** | **Storage cleanup center** | Large files, duplicate cleanup helpers, cache controls. | todo | TBD | M | mid |
| **P2** | **Offline queue visibility** | Pending exports / retries surfaced clearly. | todo | TBD | M | mid |
| **P3** | **Document security controls** | Lock/hide per doc, redact before share, optional watermark. | todo | TBD | L | long |
| **P3** | **Release readiness** | In-app feedback, changelog / what’s new, optional feature flags. | todo | TBD | M | mid |

**Effort:** `S` = small (under ~3 days), `M` = medium (~3–10 days), `L` = large (multi-week or multi-sprint).

---

## 8. Suggested implementation order

1. **Settings + notification toggles** — small scope, immediate user control.  
2. **Permissions health (P1)** — reduces setup friction early.  
3. **Trash + bulk actions (P1)** — confidence + scale for growing libraries.  
4. **Search UI enhancements** — builds on existing FTS.  
5. **Backup & recovery UX (P1)** — safety narrative before heavy sync.  
6. **Deep links + quick actions** — complements widgets and notification taps.  
7. **Document merge** — common request; medium effort.  
8. **Cloud sync** — largest effort; requires product decisions (provider, conflicts, privacy).

---

## 9. Already in place (reference)

- OCR pipeline, categories, FTS search, Room + migrations, Koin DI, MVVM-oriented structure.  
- Error/loading states (KMP + iOS), PDF preview, onboarding flow, export reminders + duplicate alerts (with ongoing notification polish).  
- UI polish pass (animations, empty state, accessibility improvements on dashboard).

Update this file when scope changes or items ship.
