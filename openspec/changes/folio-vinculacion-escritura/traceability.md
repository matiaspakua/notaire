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
| Issue | #838 | open |
| Use Case | CU87 – Vincular Escritura a Folio y Copia a Testimonio | exists (created during triage) |
| Specification | `openspec/changes/folio-vinculacion-escritura/` | in progress |
| Branch | `feat/838_folio-vinculacion-escritura` | pending |
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
| Alta de folio vinculado a una escritura | `FolioControllerTest#shouldLinkFolioToEscrituraOnCreate` | pending |
| Edición de folio para vincularlo a una escritura | `FolioControllerTest#shouldLinkFolioToEscrituraOnUpdate` | pending |
| Alta de folio sin vincular | `FolioControllerTest#shouldCreateFolioWithoutEscritura` | pending |
| Folio ya vinculado a otra escritura | `FolioControllerTest#shouldRejectLinkingFolioAlreadyUtilizadoByAnotherEscritura` | pending |
| Re-vincular el mismo folio a la misma escritura | `FolioControllerTest#shouldAllowReSavingFolioWithSameEscritura` | pending |
| Consultar un folio con escritura vinculada | `FolioControllerTest#shouldReturnEscrituraInFolioDto` | pending |
| Consultar un folio sin escritura vinculada | `FolioControllerTest#shouldReturnNullEscrituraWhenNotLinked` | pending |
| Testimonio con movimiento inscripto | `CopiaControllerTest#shouldRejectCopiaWhenTestimonioHasInscriptaMovimiento` | pending |
| Testimonio sin movimientos inscriptos | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoInscriptaMovimiento` | pending |
| Testimonio sin movimientos registrados | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoMovimientos` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU87 – Vincular Escritura a Folio y Copia a Testimonio.md` | no | — |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | in progress |
| 2 | Failing tests written, test cases designed | no | — |
| 3 | Suite green, coverage held, docs updated | no | — |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.
