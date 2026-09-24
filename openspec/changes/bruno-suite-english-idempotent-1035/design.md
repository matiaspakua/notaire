> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`backend-api/api-test/` is a Bruno OpenCollection (YAML) suite of ~150 requests,
one self-contained lifecycle folder per resource. A baseline run on 2026-09-24
against a database that had already seen earlier runs failed 21/152 requests:
the people fixtures reused document numbers 30555445–30555448 (409 on re-run) and
the fallback read `idPersonaExistente`, renamed to `existingPersonId` by the
backend translation. The unresolved `{{...}}` ids then produced invalid JSON and
cascaded 400s through `folios`, `historial` and `suplencias`.

## Goals / Non-Goals

**Goals:**
- English names everywhere the suite owns them (folders, files, requests, tests,
  variables, descriptions, free-text data, environment).
- Repeatable: N consecutive runs against the same database all pass.
- Every request traced to Use Case(s) and Requirement(s).

**Non-Goals:**
- Changing the API contract (URLs, JSON keys, domain codes such as `RECARGO`,
  `EMPLEADO`, `Nuevo`, `Cliente`, which the backend/frontend compare against).
- New endpoint coverage (#953).

## Decisions

- **Folder names follow the backend domain class names** (`ConceptController` →
  `concepts`, `ManagementStatusController` → `management-statuses`, ...), so the
  test tree reads like the code it tests.
- **Uniqueness by timestamp, not by lookup-or-reuse.** The first request of a
  folder derives its unique values from `Date.now()` in a `before-request`
  script. Reusing an existing row on 409 (the old approach) hid leaks and broke
  when the 409 body changed.
- **Each folder deletes what it creates**, in reverse dependency order (child →
  parent → fixture person). Seed rows (person 1, procedure type 1, folio type 1,
  identification type 1, management statuses 1/2) are only referenced, never
  mutated.
- **Traceability in `info.description`** (`Traceability: CU29 (RF #55, #60)`),
  so it shows up in Bruno's UI and HTML report next to the request.

## Riesgos / Trade-offs

- [Risk] Renaming folders breaks `CU-API-MATRIX.csv` paths → Mitigation: matrix
  `Bruno_Test` column updated in the same PR.
- [Risk] CI/preflight still pass `--env Developmen` → Mitigation: both updated in
  the same PR (CLAUDE.md: gate changes must keep preflight and CI in sync).
- [Risk] Timestamp-derived 8-digit document numbers repeat every ~28 h →
  Mitigation: every person fixture is deleted in the same run, so no row is left
  to collide with.

## Testing Strategy

The change *is* the test suite; its acceptance test is running it.

| Scenario (acceptance criterion) | Test level | Test class / file |
|---------------------------------|------------|-------------------|
| Suite passes on the current database | API (Bruno) | `bru run . -r --env Development` |
| Suite passes again immediately after (idempotent) | API (Bruno) | second consecutive `bru run` |
| No fixture rows leaked | DB check | row counts of `people`, `budgets`, `deed_managements`, `users`, `folios` before/after |

- New unit tests: none
- New integration tests: none (Bruno requests only)
- Coverage impact: none on JaCoCo (no Java change)

## Regression Strategy

- Existing tests affected: every Bruno request (renamed/rewritten); assertions kept
  or strengthened, none removed.
- Full suite command: `cd backend-api/api-test && bru run . -r --env Development`,
  run twice.
- Legacy paths at risk: none.

## Playwright Strategy

n/a — no UI surface; no frontend file is touched.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none — tests only
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI "API Tests (Bruno)" job green on `main`.

## Rollback Strategy

- Revert safe: yes — `git revert` restores the previous collection and env name.
- Database rollback: none.
- Data written under the new behavior after revert: none (the suite cleans up).
- Blast radius if rollback is delayed: none.

## Migration Plan

n/a — single step.
