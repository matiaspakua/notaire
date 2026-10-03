> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

PR #1201 Englishized #804-touched Java/OpenAPI strings but left the committed
OpenAPI artifact stale, omitted an OpenSpec change folder, and left one
integration test still expecting PUT status change `200` plus Playwright
matchers for Spanish `no está permitida`.

## Goals / Non-Goals

**Goals:**

- Process Checks + OpenAPI Contract + Integration/Coverage green on #1201.
- Playwright edge paths that assert transition-rejected copy match English
  `is not allowed`.
- Keep URL/JSON Spanish deferred.

**Non-Goals:**

- New workflow behavior; ADR path rename; touching PR #1202.

## Decisions

1. **Track as #1203** — #804 is closed/completed and cannot be reopened by the
   agent token; Process Checks need an open issue for `validate-sdlc-plan.sh`.
2. **`skip_specs: true`** — no requirement delta; only message language and
   test alignment. Rejected inventing a fake capability.
3. **Tie-date integration test seeds History in-repo** — same pattern as
   `shouldReturnEstadoActualFromHistoryWhenRowsExist`, because #804 rejects
   status change via PUT.
4. **Playwright match `/is not allowed/i`** — mirrors backend
   `BusinessValidationException` text after Englishize.

## Riesgos / Trade-offs

- [Clients matching Spanish error substrings break] → intentional per English
  hygiene; E2E updated in this change.
- [OpenAPI tag rename `Gestiones`→`Managements`] → docs-only tag; paths unchanged;
  regenerate artifact in same PR.

## Testing Strategy

| Scenario (acceptance) | Test level | Test class / file |
|-----------------------|------------|-------------------|
| Invalid transition message is English | unit | `TransitionManagementUseCaseTest` / `ManagementTransitionServiceTest` |
| PUT status change rejected (400) | integration | `ManagementHistorialOrphanWriteIntegrationTest` |
| estado-actual tie-break with seeded History | integration | `shouldReturnLaterHistoryRowWhenDatesTie` |
| Archive without transition shows English error | E2E | `TS-0011`, `TS-0028` |
| OpenAPI committed matches export | CI | OpenAPI Contract workflow |

- New unit tests: none required (existing unit tests already Englishized)
- New integration tests: none — fix existing
- Coverage impact: none expected (behavior unchanged)

## Regression Strategy

- Existing tests affected: `ManagementHistorialOrphanWriteIntegrationTest`
  (tie-date setup); Playwright `TS-0011`, `TS-0028` error matchers.
- Full suite command: `mvn test -pl backend-api -Dtest=ManagementHistorialOrphanWriteIntegrationTest`
- HTTP/Bruno API suite: unchanged paths; Bruno already green on #1201.
- Legacy paths at risk: none intentionally.

## Playwright Strategy

- Specs to update: `frontend/tests/e2e/TS-0011-gestiones-crud-workflow.spec.ts`,
  `frontend/tests/e2e/TS-0028-gestion-historial-feature.spec.ts`
- Golden path: unchanged (workflow transitions still UI-driven)
- Edge / error paths: archive without Archivada transition asserts English copy
- Viewports: existing suite defaults
- Command: `cd frontend && npx playwright test TS-0011-gestiones-crud-workflow TS-0028-gestion-historial-feature`

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single draft PR #1201 push
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): n/a until merge — CI green on PR

## Rollback Strategy

- Revert the follow-up commit(s) on the branch / close PR. Safe: no schema or
  URL contract change. Restores Spanish messages and stale OpenAPI if reverted
  fully.
