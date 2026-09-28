# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue #883 → spec.md → plan.md → tasks.md → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #883 | closed |
| Use Case | CU01 — Preparar Presupuesto | exists |
| Specification | `speckit/specs/archive/004-cu01-presupuesto-persona-fix/` | archived |
| Branch | `fix/883_presupuesto-persona-association` | merged, deleted |
| Tasks | `tasks.md` | all groups done |
| Commits | `376f736`, `2468da8`, `7a355ab`, `83949f3`, `693c5ed`, `71dcf9a`, `af033ed`, `5f6fa72` | done |
| Pull Request | [#887](https://github.com/matiaspakua/notaire/pull/887) | merged |
| CI run | all checks green on PR #887 | passed |
| Merge commit | `055e8e7267d16a317d75208049dccac67d48d716` | merged to `main` |
| Release / tag | none planned — continuous deploy off `main`, same as #879/#865 | n/a |
| Smoke test | `POST`/`GET` round trip against rebuilt `main` — `201` create with `persona.idPersona`, `GET` returns populated `persona`; test data cleaned up | passed |

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
| 4 | CI green, review approved, no conflicts | yes | PR #887 checks all green, merged |
| 5 | Deployed, smoke test passed, Issue closed | yes | Rebuilt `main` Docker smoke test passed, issue #883 auto-closed by merge |

## Exceptions

None taken.
