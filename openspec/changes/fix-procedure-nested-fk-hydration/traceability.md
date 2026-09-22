# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #981 | in-progress |
| Use Case | CU82 — Generar Minuta de Inscripción | exists (`docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md`) |
| Specification | `openspec/changes/fix-procedure-nested-fk-hydration/` | complete |
| Branch | `fix/981_procedure_nested_fk_hydration` | created |
| Tasks | `tasks.md` | groups 1-9 complete; 10-12 pending PR/deploy |
| Commits | `a6e6cdc` (planning), `00aa187` (failing tests), `fe646a5` (implementation), `ab22527` (tramites_personas fix), `a520d25` (test updates) | committed, not yet pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Creating a Procedure with a real Deed id returns the Deed's actual state | `ProcedureControllerFkHydrationIntegrationTest.shouldReturnRealDeedStateOnCreate` | passing |
| A subsequent GET reflects the same real association state | `ProcedureControllerFkHydrationIntegrationTest.shouldReflectRealDeedStateOnSubsequentGet` | passing |
| idProcedureType is required | `ProcedureControllerFkHydrationIntegrationTest.shouldRejectMissingProcedureType` | passing |
| A non-existent referenced id is rejected | `ProcedureControllerFkHydrationIntegrationTest.shouldRejectNonExistentDeed` | passing |
| Updating a Procedure with a real Deed id returns the Deed's actual state | `ProcedureControllerFkHydrationIntegrationTest.shouldReturnRealDeedStateOnUpdate` | passing |

Additional verification beyond the delta spec's own scenarios:
- `mvn verify -pl backend-api`: 1041/1041 tests green, Checkstyle clean, JaCoCo ratchet held
- Bruno `tramites` folder: 34/34 (auth + full CRUD lifecycle) against a clean Docker/Postgres stack
- Playwright `TS-0082-minuta-inscripcion-feature.spec.ts`: 6/6 (golden path + 2 edge cases + 2 viewport checks), previously blocked by this bug

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` — `[Unreleased]` → `### Fixed` | yes | pending push |
| Swagger/OpenAPI | auto-updates from the new `ProcedureRequest` record, no manual edit needed | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | proposal.md + spec.md written, validated by `scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | passed | `00aa187` — 5/5 failed against old `ProcedureController` before implementation |
| 3 | Suite green, coverage held, docs updated | passed | 1041/1041 local, Checkstyle clean, CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None taken. One unplanned but in-scope discovery: while verifying this
fix end-to-end against a real Postgres instance (`DELETE /api/v1/tramites/{id}`
via Bruno), found that `Procedure.java`'s own `@JoinTable` for `personList`
still declared the stale table name `tramites_personas` — a regression
missed during the domain-schema-to-English epic (#973, Slice 6, `V31`,
already merged to `main`). Fixed in the same change (`ab22527`) since it
directly blocked verifying this fix's own delete path, rather than opening
a separate issue for something already found and fixed.
