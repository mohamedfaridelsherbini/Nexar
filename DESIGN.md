---
version: alpha
name: Nexar
description: Native document-scanning product design system for Android and iOS, formatted to align with the google-labs-code DESIGN.md specification.
colors:
  primary: "#0F172A"
  secondary: "#475569"
  tertiary: "#0EA5A4"
  neutral: "#F8FAFC"
  surface: "#FFFFFF"
  border: "#CBD5E1"
  success: "#15803D"
  warning: "#EA580C"
  error: "#B91C1C"
  on-primary: "#FFFFFF"
  on-secondary: "#FFFFFF"
  on-tertiary: "#042F2E"
  on-neutral: "#0F172A"
  on-surface: "#111827"
  on-error: "#FFFFFF"
typography:
  h1:
    fontFamily: SF Pro Display
    fontSize: 32px
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: -0.02em
  h2:
    fontFamily: SF Pro Display
    fontSize: 24px
    fontWeight: 700
    lineHeight: 1.15
    letterSpacing: -0.01em
  title-md:
    fontFamily: SF Pro Display
    fontSize: 18px
    fontWeight: 600
    lineHeight: 1.25
    letterSpacing: 0em
  body-md:
    fontFamily: SF Pro Text
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: 0em
  body-sm:
    fontFamily: SF Pro Text
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.45
    letterSpacing: 0em
  label-md:
    fontFamily: SF Pro Text
    fontSize: 13px
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: 0.01em
rounded:
  sm: 12px
  md: 16px
  lg: 24px
  full: 999px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
components:
  screen:
    backgroundColor: "{colors.neutral}"
    textColor: "{colors.on-neutral}"
    padding: "{spacing.md}"
  topbar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.h2}"
    padding: "{spacing.md}"
  card-document:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: "{spacing.md}"
  banner-warning:
    backgroundColor: "{colors.warning}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.md}"
    padding: "{spacing.md}"
  fab-scan:
    backgroundColor: "{colors.tertiary}"
    textColor: "{colors.on-tertiary}"
    typography: "{typography.title-md}"
    rounded: "{rounded.full}"
    height: 56px
    padding: 16px
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    rounded: "{rounded.full}"
    padding: 12px
  input-search:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.full}"
    padding: 14px
  icon-tile:
    backgroundColor: "{colors.tertiary}"
    textColor: "{colors.on-tertiary}"
    rounded: "{rounded.sm}"
    size: 52px
---

## Overview

Nexar should feel precise, modern, and dependable, like a professional scanning tool rather than a generic utilities demo. The interface should communicate clarity first: strong hierarchy, quiet backgrounds, compact actions, and obvious document affordances. The mood is closer to a trusted productivity instrument than to a playful consumer app.

The product handles capture, file organization, export, and preview. That means the design should support concentration and reduce hesitation. The user should always understand:

- what their latest scans are
- where exports go
- which action is primary
- whether they are in a safe local workflow or an external storage workflow

Visual noise should stay low. Accent usage should stay intentional and rare, mostly for the scan action, active controls, and key affordances.

## Colors

The palette is built around cool, high-legibility neutrals with a single scanning/export accent.

- **Primary (`#0F172A`)**: the anchor tone for titles, high-value text, and structural emphasis.
- **Secondary (`#475569`)**: supporting metadata, helper text, and secondary iconography.
- **Tertiary (`#0EA5A4`)**: the action accent for scanning, key selection states, and active affordances.
- **Neutral (`#F8FAFC`)**: the page foundation. It should feel clean without becoming harsh.
- **Surface (`#FFFFFF`)**: cards, grouped list rows, sheets, and fields.
- **Border (`#CBD5E1`)**: separators, subtle outlines, and quiet boundaries.
- **Warning (`#EA580C`)**: configuration gaps such as missing export folder state.
- **Error (`#B91C1C`)**: destructive and failure messaging only.

Color behavior:

- default screens should read as light and crisp
- most components should sit on `neutral` or `surface`
- the tertiary accent should not compete with every action; reserve it for the main action path
- warnings should be visible but not alarmist

## Typography

Typography should feel native on Apple platforms and clean on modern mobile surfaces generally. The system should prefer dense but breathable information display.

- `h1` is for large hero or empty-state emphasis only
- `h2` is for screen titles and major section anchors
- `title-md` is for document names and action labels with moderate emphasis
- `body-md` is the default reading and form text
- `body-sm` is for metadata, helper copy, and status text
- `label-md` is for compact button labels, chips, and utility controls

Type behavior:

- use larger display sizes sparingly
- document names should be clear before timestamps or metadata
- avoid decorative font mixing
- keep line lengths short and task-oriented

## Layout

Layout should prioritize one-handed scanning workflows and quick list review.

- use a light grouped structure rather than heavy dashboard chrome
- maintain comfortable horizontal padding with `spacing.md`
- primary vertical rhythm should be driven by `spacing.md` and `spacing.lg`
- floating or bottom-anchored primary actions should remain visually dominant without overwhelming the content
- empty states should have generous breathing room and simple messaging

List behavior:

- documents should appear in a vertically stacked, card-like or inset-grouped rhythm
- previews should be opened from the row tap target, while rename/export remain explicit side actions
- storage state should appear above the list as a persistent banner or status block

## Elevation & Depth

Depth should be subtle.

- cards may use soft separation through contrast before shadow
- floating primary actions may use moderate shadow to signal interactivity
- alerts, sheets, and full-screen capture flows should rely on native platform depth behavior
- avoid layered surfaces competing for attention on the same screen

The design should feel crisp and flat-first, with only targeted depth cues where they improve action discoverability.

## Shapes

Nexar should use soft geometry rather than sharp enterprise corners.

- smaller utility surfaces use `rounded.sm`
- cards, banners, and grouped containers use `rounded.md`
- large call-to-action surfaces may use `rounded.full`
- avoid mixing many corner radii in one view

Shapes should make the app feel approachable and polished without becoming playful.

## Components

### Screen

- screens use `components.screen`
- background should remain `neutral`
- avoid dark or heavily tinted full-screen fills

### Top Bar

- top bars use `components.topbar`
- titles should feel stable, not oversized
- toolbar icons should be minimal and utilitarian

### Document Card / Row

- document rows use `components.card-document`
- each row should clearly separate:
  - document icon
  - document title
  - page count or metadata
  - quick actions

The icon tile should use `components.icon-tile` as the most colorful part of the row, with the rest staying restrained.

### Warning Banner

- missing storage configuration should use `components.banner-warning`
- the warning state should be highly visible but still feel integrated into the product

### Scan FAB / Primary Scan Action

- the scan action uses `components.fab-scan`
- it should be the clearest action on the screen
- it should remain distinct from export and rename controls

### Secondary Buttons

- export-folder and utility actions may use `components.button-secondary`
- they should feel lighter than the scan action

### Search Input

- search uses `components.input-search`
- it should feel integrated into the content surface, not like a heavy admin filter

## Do's and Don'ts

Do:

- keep the main scan action visually dominant
- use the accent color sparingly and consistently
- preserve clear hierarchy between document title and metadata
- keep surfaces bright, clean, and lightly separated
- prefer native-feeling controls and restrained motion

Don't:

- flood the interface with multiple accent colors
- make export, rename, and scan feel equally weighted
- overuse shadows, gradients, or glass effects
- use cramped list rows with tiny tap targets
- turn error or warning colors into general-purpose decoration
