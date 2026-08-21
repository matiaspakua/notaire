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
| Specification | `openspec/changes/persona-validacion-duplicados/` | in progress |
| Branch | `feat/835_persona-validacion-duplicados` | not created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta exitosa con documento no registrado | `PersonaServiceTest#shouldCreatePersonaWhenDocumentNotRegistered` | pending |
| Rechazo de alta con documento ya registrado | `PersonaServiceTest#shouldRejectCreateWhenDocumentAlreadyRegistered` | pending |
| Edición exitosa sin cambiar el documento | `PersonaServiceTest#shouldUpdatePersonaWithoutChangingDocument` | pending |
| Rechazo de edición hacia un documento de otra persona | `PersonaServiceTest#shouldRejectUpdateWhenDocumentBelongsToAnotherPersona` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU17 – Dar Alta persona.md` | no | pending |
| `docs/100-business/102-use-cases/CU18 – Dar Alta Cliente.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | — |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
