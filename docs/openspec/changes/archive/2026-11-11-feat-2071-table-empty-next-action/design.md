> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`EmptyState` already accepts `description` and `action` (#1942). `DataTable`
only forwards `emptyMessage` as the title. People, managements, and budgets each
already expose a header create button wired to `openCreate`.

## Goals / Non-Goals

**Goals:**
- Shared table empty path can show description + one action without per-page layout.
- Pilot people, managements, and budgets with real create actions already on those pages.
- Prove with a Vitest (or Playwright) that a description appears for at least one list / DataTable empty.

**Non-Goals:**
- New create modals or routes
- Icon redesign
- Empty actions on lists that have no create control

## Decisions

1. **Forward props, do not reimplement layout** — add optional `emptyDescription`
   and `emptyAction` on `DataTable`; `TableEmpty` passes them to `EmptyState`.
2. **Reuse existing create handlers** — empty action buttons call the same
   `openCreate` as the page header; do not invent alternate flows.
3. **i18n keys per module** — add `emptyDescription` (or equivalent) under
   `personas`, `gestiones`, and `presupuestos` in `en.json` / `es.json`.
4. **Unit test first** — extend `data-table.test.tsx` to assert description +
   action; optionally assert people page empty copy when the list is empty.

## Riesgos / Trade-offs

- [Risk] Duplicate create controls (header + empty) → Mitigation: intentional UX;
  both share `openCreate`.
- [Risk] Other lists remain title-only → Mitigation: out of scope; pattern is
  documented for follow-on Issues.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| DataTable empty forwards description and action | unit | `data-table.test.tsx` |
| People list empty shows description (when empty) | unit | `people-empty-next-action.test.tsx` or page test |

- New unit tests: frontend Vitest only
- New integration tests: n/a
- Coverage impact: frontend Vitest; JaCoCo unchanged

## Regression Strategy

- Existing tests affected: DataTable empty / mobile empty / people page mocks
- Full suite command: `bash workspace/sdlc/preflight.sh --full`
- HTTP/Bruno API suite: n/a (frontend-only)
- Legacy paths at risk: none

## Playwright Strategy

- Specs to add/update: n/a if Vitest covers the AC; existing list smoke remains
- Golden path: empty DataTable shows description when provided
- Edge / error paths: error branch unchanged (#1947)
- Viewports: covered by existing DataTable mobile unit tests
- Command: `cd testing/e2e && npx playwright test` via preflight `--full`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: frontend-only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): open people / managements / budgets empty lists

## Rollback Strategy

- Revert safe: yes — presentational + copy only
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: empty lists slightly less guided

## Migration Plan

1. Fail Vitest for DataTable description/action forwarding.
2. Wire DataTable → EmptyState.
3. Add i18n + pilot three list pages.
4. Green tests + permanent docs + preflight `--full`.

## Open Questions

(none)
