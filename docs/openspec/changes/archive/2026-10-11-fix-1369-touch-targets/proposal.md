# Touch targets of at least 44x44 on touch screens (#1369)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1369 |
| Use Case | RNF-06 – Diseño de ventanas; RNF-07 – Diseño de campos y combos |
| Branch | `fix/1369_touch_targets` |
| Gate 1 status | draft |

## Objetivo

Row actions, filters and pagination use `Button size="sm"` (32px tall, icon-only ones 32px wide) and sit `gap-1` (4px) apart, and the rows-per-page select is 36px. On phones and tablets they are below the 44x44 minimum (WCAG 2.5.5 / Apple HIG), so taps miss or hit the neighbouring delete button. The fix is made once in the shared components, so every list page benefits and the desktop look is unchanged.

## What Changes

- `components/ui/button.tsx`: `size="sm"` adds `pointer-coarse:h-11 pointer-coarse:min-w-11` (44x44 minimum only when the primary pointer is coarse); new `size="icon-sm"` (32px square on desktop, 44px on touch) for icon-only actions.
- `components/shared/Pagination.tsx`: rows-per-page select `pointer-coarse:h-11`; page buttons `gap-2`.
- `administracion/workflows/page.tsx`: row actions `gap-2` (the only `gap-1` container of small buttons left).
- Guards: Vitest `button.test.tsx` (size classes, no `gap-1` container holding small buttons, select height); Playwright `TS-0122-touch-targets.spec.ts`.
- CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Interactive targets are at least 44x44 CSS px on touch devices; 8px between adjacent row actions | WCAG 2.5.5, Apple HIG; #1369 | New |
| Desktop (fine pointer) keeps the compact 32px small buttons | #1369 acceptance criteria | Made explicit |

## Capabilities

### New Capabilities

- `ui-touch-targets`: Interactive controls meet the 44x44 minimum on touch screens.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | button variants, Pagination, workflows row actions |
| `testing/e2e` | yes | TS-0122 |
| `backend-api` | no | — |

### Surface area

- Endpoints / entities / configuration: none

### Architecture review

No architecture change: presentation only, through the shared button variants.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
