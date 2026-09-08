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
| Issue | #838 | in-progress |
| Use Case | CU87 – Vincular Escritura a Folio y Copia a Testimonio | exists (created during triage) |
| Specification | `openspec/changes/folio-vinculacion-escritura/` | complete |
| Branch | `feat/838_folio-vinculacion-escritura` | active |
| Tasks | `tasks.md` | implementation + tests + docs complete; PR pending |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | local `mvn verify` + `preflight.sh --full` green (2026-09-08) | pending (remote) |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | `docker compose build` + smoke test green locally (2026-09-08) | pending (post-deploy) |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta de folio vinculado a una escritura | `FolioControllerTest#shouldLinkFolioToEscrituraOnCreate` | done |
| Edición de folio para vincularlo a una escritura | `FolioControllerTest#shouldLinkFolioToEscrituraOnUpdate` | done |
| Alta de folio sin vincular | `FolioControllerTest#shouldCreateFolioWithEstadoNuevo` | done |
| Folio ya vinculado a otra escritura | `FolioControllerTest#shouldRejectLinkingFolioAlreadyUtilizadoByAnotherEscritura` | done |
| Re-vincular el mismo folio a la misma escritura | `FolioControllerTest#shouldAllowReSavingFolioWithSameEscritura` | done |
| Consultar un folio con escritura vinculada | `FolioTest#shouldRoundTripEscrituraThroughDto` | done |
| Consultar un folio sin escritura vinculada | `FolioTest#shouldReturnNullEscrituraWhenNotLinked` | done |
| Testimonio con movimiento inscripto | `CopiaControllerTest#shouldRejectCopiaWhenTestimonioHasInscriptaMovimiento` | done |
| Testimonio sin movimientos inscriptos | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoInscriptaMovimiento` | done |
| Testimonio sin movimientos registrados | `CopiaControllerTest#shouldCreateCopiaWhenTestimonioHasNoMovimientos` | done |
| E2E golden path + edge cases (UI) | `frontend/tests/e2e/folios-vinculacion.spec.ts` (7 tests) | done |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU87 – Vincular Escritura a Folio y Copia a Testimonio.md` | yes — GitHub ID, curso de eventos and excepciones corrected to match `Utilizado` estado and copia rejection (not preservation) | pending |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | yes — cross-reference to CU87 added | pending |
| `CHANGELOG.md` | yes — `[Unreleased]` entry added | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #838, `proposal.md`, `specs/*/spec.md` |
| 2 | Failing tests written, test cases designed | yes | Requirement coverage table above |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` BUILD SUCCESS (1814 tests); `preflight.sh --full` green except 2 pre-existing unrelated E2E flakes (confirmed passing in isolation) and 1 unrelated Bruno CLI QuickJS crash (268/268 assertions passed) |
| 4 | CI green, review approved, no conflicts | no | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | no | pending merge |

## Exceptions

None.
