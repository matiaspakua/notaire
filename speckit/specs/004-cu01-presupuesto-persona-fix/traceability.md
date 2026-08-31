# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #883 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #883 | in-progress |
| Use Case | CU01 — Preparar Presupuesto | exists |
| Specification | `speckit/specs/004-cu01-presupuesto-persona-fix/` | in progress |
| Branch | `fix/883_presupuesto-persona-association` | created |
| Tasks | `tasks.md` | TDD/implementation/regression/E2E/docs done; PR/merge/Gate 5 pending |
| Commits | `610d5ed`, `946ad41`, `d934ced`, `fb2c52c` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | none planned — continuous deploy off `main`, same as #879/#865 | n/a |
| Smoke test | pending — will be `POST`/`GET` round trip against rebuilt `main`, per `tasks.md` Gate 5 | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| US1 scenario 1 — create with `persona` → linked | `PresupuestoPersonaAssociationPgIntegrationTest#shouldPersistPersonaAssociationOnCreate` | passing |
| US1 scenario 2 — edit to add `persona` → linked | `PresupuestoPersonaAssociationPgIntegrationTest#shouldPersistPersonaAssociationOnUpdate` | passing |
| US1 scenario 3 — omit `persona` → still succeeds | `PresupuestoPersonaAssociationPgIntegrationTest#shouldCreateWithoutPersonaWhenOmitted` | passing |
| US2 scenario 1 — `GET /{id}` returns `persona` | `PresupuestoPersonaAssociationPgIntegrationTest#shouldReturnPersonaFieldOnGetById` | passing |
| US2 scenario 2 — list reflects presence/absence correctly | `PresupuestoPersonaAssociationPgIntegrationTest#shouldReflectPersonaAcrossListedPresupuestos` | passing |
| E2E golden path | `frontend/tests/e2e/cu01-presupuesto-persona.spec.ts` | passing (2/2) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | `fb2c52c` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + spec.md + Acceptance Criteria | yes | Issue #883, `spec.md` Notaire Traceability + Given/When/Then scenarios, `bash scripts/validate-speckit-plan.sh 004-cu01-presupuesto-persona-fix` passing |
| 2 | Failing tests written, test cases designed | yes | `PresupuestoPersonaAssociationPgIntegrationTest` written first (TDD), confirmed red against pre-fix code, then green (5/5) after the `@JsonProperty` fix |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` (787 tests, 0 failures), `mvn test -Ppg-integration` (5/5), Playwright spec (2/2, full suite 319 passed/37 skipped), `CHANGELOG.md` |
| 4 | CI green, review approved, no conflicts | pending | PR checks, `mergeStateStatus: CLEAN` |
| 5 | Deployed, smoke test passed, Issue closed | pending | Rebuilt `main` smoke test, `gh issue close 883` |

## Exceptions

None taken.
