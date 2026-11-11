# Table empty states name the gap and the next action

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2071 |
| Use Case | RNF-05 — Aspecto visual |
| Branch | `fix/2071_table-empty-next-action` |
| Gate 1 status | passed |

## Objetivo

`DataTable` already renders shared `EmptyState` (#1942) but only forwards a title.
List screens therefore stop at “nothing here” even when a create action already
exists on the page. Follow-on to #1942 ranked P1 in the 2026-11-07 Operate UI pass.

## What Changes

- Extend `DataTable` so callers can pass an optional empty description and one
  empty action through to `EmptyState` (desktop table and mobile cards).
- People, managements, and budgets lists pass a description and reuse their
  existing create (`openCreate`) control as the empty action when present.
- Add i18n copy for those empty invitations; document the DataTable props;
  Vitest covers description forwarding (and one list when practical).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Empty lists name the gap and teach the next step when a create action already exists | RNF-05, epic #1941 / #1942 follow-on | Made explicit for DataTable empty rows |
| Do not invent create flows that do not already exist on the page | Issue #2071 out of scope | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `ui-page-chrome`: DataTable empty state MAY forward description and action to EmptyState; pilot lists pass them.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | yes | DataTable props + people/managements/budgets + i18n + tests |
| `frontend-swing` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github/workflows`) | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- BREAKING: no

### Write set

- `frontend/src/components/shared/DataTable.tsx`
- `frontend/src/app/dashboard/people/page.tsx`
- `frontend/src/app/dashboard/managements/page.tsx`
- `frontend/src/app/dashboard/budgets/page.tsx`
- `frontend/messages/en.json`
- `frontend/messages/es.json`
- `frontend/src/tests/unit/**`
- `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md`
- `docs/openspec/changes/feat-2071-table-empty-next-action/**`
- `docs/openspec/specs/ui-page-chrome/**`
- `CHANGELOG.md`

### Architecture review

Follows ADR-011 tokens and existing EmptyState API. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/203-design/FRONTEND-DESIGN-SYSTEM.md` | Document DataTable `emptyDescription` / `emptyAction` forwarding |
| `CHANGELOG.md` | User-visible empty invitation improvement |

## Out of Scope

- Inventing create flows that do not exist on a list page
- Changing the empty-state icon set
- Migrating every DataTable consumer beyond people / managements / budgets
- Wilson / portal / Context Lake / `[committer] required=true`
