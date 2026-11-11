# Mobile nav group toggles meet the 44px touch target (#2074)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2074 |
| Use Case | RNF-05 — Aspecto visual |
| Branch | `fix/2074_mobile-nav-group-targets` |
| Gate 1 status | passed |

## Objetivo

`AppSidebar` group disclosure buttons use `min-h-8` (32px). Below 768px the
mobile sheet is the only nav, so those toggles are touch targets. The product's
large controls are already 44–48px; raise the mobile sheet group toggles to at
least 44px while keeping the desktop sidebar compact.

## What Changes

- `AppSidebar`: mobile-sheet group toggles use a ≥44px hit area (`min-h-11`);
  desktop group toggles keep the compact `min-h-8` density.
- Vitest `app-sidebar.test.tsx`: assert mobile vs desktop min-heights.
- Playwright `TS-0121` still passes (grouped sidebar behaviour unchanged).
- CHANGELOG entry under `[Unreleased]`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Mobile-sheet group disclosure toggles are at least 44 CSS px tall | RNF-05 / #2074; product large-control size | New |
| Desktop sidebar group toggles may stay compact (32px) | #2074 acceptance | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-touch-targets`: extend coverage to `AppSidebar` mobile-sheet group toggles.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `frontend` | yes | `AppSidebar` group toggle classes; Vitest |
| `testing/e2e` | no | TS-0121 regression only (no new assertion required) |
| `backend-api` | no | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `frontend/src/components/layout/AppSidebar.tsx`
- `frontend/src/tests/unit/app-sidebar.test.tsx`
- `CHANGELOG.md`
- `docs/openspec/changes/fix-2074-mobile-nav-group-targets/**`
- `docs/openspec/changes/archive/**/fix-2074-mobile-nav-group-targets/**`
- `docs/openspec/specs/ui-touch-targets/**`

### Architecture review

Presentation-only. Reuses the existing desktop/mobile dual render in
`AppSidebar` (`idPrefix`) so the mobile sheet can grow the hit area without
changing the desktop column density. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed / Changed entry for the larger mobile group toggles |
| `docs/openspec/specs/ui-touch-targets/spec.md` | Sync delta on archive |

## Out of Scope

- Rewriting the information architecture of the nav groups (#1367).
- Wilson / portal / Context Lake / `[committer] required=true`.
- Changing close-button size or other chrome outside group toggles.
