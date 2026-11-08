# Shared PageHeader, EmptyState, and PageSection primitives

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1942 |
| Use Case | RNF-09 – Uso de colores en la GUI |
| Branch | `feat/1942_page-chrome-primitives` |
| Gate 1 status | passed |

## Objetivo

Dashboard surfaces hand-roll titles, empty placeholders, and section rhythm, so the
product does not share one Apple/Revolut/SpaceX chrome language. Parent epic #1941
Wave 2 introduces three token-backed primitives and pilots them on home + list pages.

## What Changes

- Add `PageHeader`, `EmptyState`, and `PageSection` under `frontend/src/components/shared/`.
- `AppHeader` composes `PageHeader` (sticky glass chrome unchanged).
- `DataTable` empty rows/cards render `EmptyState` (optional action slot).
- Pilots: dashboard home, people list, managements list — no decorative ALL-CAPS labels
  on those surfaces where this change touches typography.
- Document in `FRONTEND-DESIGN-SYSTEM.md`; CHANGELOG entry; Vitest + Playwright smoke.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| UI colour and chrome come from the design system | RNF-09, ADR-011 | Made explicit for page chrome |
| Empty lists invite a next action when one exists | UX (epic #1941) | New for EmptyState |

## Capabilities

### New Capabilities

- `ui-page-chrome`: shared PageHeader, EmptyState, and PageSection behaviour.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | shared chrome + pilots + tests |
| `frontend-swing` | no | – |
| `infra` / observability | no | – |
| CI/CD | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database: none
- Configuration: none
- Dependencies: none (lucide already present)
- BREAKING: no

### Architecture review

Follows ADR-011 tokens. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Document the three primitives + usage |
| `CHANGELOG.md` | Added/Changed entry |

## Out of Scope

- Migrating every dashboard page to PageSection (only pilots)
- Charts / workflow diagram polish (later epic waves)
- a11y axe suite (#1251)
