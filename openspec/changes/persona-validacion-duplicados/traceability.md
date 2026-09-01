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
| Issue | #835 | open |
| Use Case | CU17 – Dar Alta persona (#170); CU18 – Dar Alta Cliente (#171) | exists |
| Specification | `openspec/changes/persona-validacion-duplicados/` | complete |
| Branch | `feat/835_persona-validacion-duplicados` | created |
| Tasks | `tasks.md` | groups 1-8 complete (9-12 pending) |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

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
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | yes | pending (commit not yet made) |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | no — existing generic exception text (7.2) already covers this case | n/a |
| `CHANGELOG.md` | yes | pending (commit not yet made) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | passed | Issue #835 open, spec.md with 4 scenarios, `validate-sdlc-plan.sh` green for this change |
| 2 | Failing tests written, test cases designed | passed | 4 `PersonaServiceTest` cases + 1 `PersonaRequestValidationIntegrationTest` case + 4 Playwright scenarios |
| 3 | Suite green, coverage held, docs updated | passed | `mvn test -pl backend-api` (only pre-existing, unrelated failures: pago/saldo-pendiente tests), `jacoco:check@check` green, Playwright green, CU17/CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
