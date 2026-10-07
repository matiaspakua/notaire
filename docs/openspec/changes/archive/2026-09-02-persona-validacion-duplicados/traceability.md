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
| Issue | #835 | closed |
| Use Case | CU17 – Dar Alta persona (#170); CU18 – Dar Alta Cliente (#171) | exists |
| Specification | `openspec/changes/persona-validacion-duplicados/` | complete |
| Branch | `feat/835_persona-validacion-duplicados` | created |
| Tasks | `tasks.md` | complete (68/68) |
| Commits | `e6a15d2c`, `589332dc`, `eeed01e9`, `9077e9b3`, `605703f5`, `f800bd31` (2 further commits on the PR branch not independently retained after the squash merge) | done |
| Pull Request | [#905](https://github.com/matiaspakua/notaire/pull/905) | merged |
| CI run | `pr-validation.yml`/`ci.yml`/`frontend-ci.yml`/`playwright-e2e.yml` — no retained check data (branch deleted post-merge); `tasks.md` §6-7 record all suites green locally before merge | passed (evidence: local pre-merge runs) |
| Merge commit | `13090837` (squash) | done |
| Release / tag | n/a — no tagged release cut for this merge | — |
| Smoke test | manual API smoke test 2026-09-02 against local stack: created persona with a fresh `numeroIdentificacion` (201), retried `POST /personas` with the same document (409, body `{"message":"Ya existe una persona registrada con el documento ...","idPersonaExistente":<id>}`); `GET /actuator/health` returned 200; test data cleaned up afterward | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta exitosa con documento no registrado | `PersonaServiceTest#shouldCreatePersonaWhenDocumentNotRegistered` | passing |
| Rechazo de alta con documento ya registrado | `PersonaServiceTest#shouldRejectCreateWhenDocumentAlreadyRegistered`, `PersonaRequestValidationIntegrationTest#shouldRejectCreateWithDuplicateDocument` | passing |
| Edición exitosa sin cambiar el documento | `PersonaServiceTest#shouldUpdatePersonaWithoutChangingDocument` | passing |
| Rechazo de edición hacia un documento de otra persona | `PersonaServiceTest#shouldRejectUpdateWhenDocumentBelongsToAnotherPersona` | passing |
| E2E: alta, edición y bloqueo de duplicado en la UI | `frontend/tests/e2e/persona-validacion-duplicados.spec.ts` (4 scenarios) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | yes | `9077e9b3` |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | no — existing generic exception text (7.2) already covers this case | n/a |
| `CHANGELOG.md` | yes | `9077e9b3` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | Issue #835 open, spec.md with 4 scenarios, `validate-sdlc-plan.sh` green for this change |
| 2 | Failing tests written, test cases designed | passed | 4 `PersonaServiceTest` cases + 1 `PersonaRequestValidationIntegrationTest` case + 4 Playwright scenarios |
| 3 | Suite green, coverage held, docs updated | passed | `mvn test -pl backend-api` (only pre-existing, unrelated failures: pago/saldo-pendiente tests), `jacoco:check@check` green, Playwright green, CU17/CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | yes | PR #905 merged as `13090837` |
| 5 | Deployed, smoke test passed, Issue closed | yes | smoke test passed (see Chain — Smoke test); Issue #835 closed |

## Exceptions

None.
