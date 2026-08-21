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
| Issue | #837 | open |
| Use Case | CU27 – Ingresar nuevo tipo de documento (#180); CU32 – Modificar tipo de documento (#185); CU42 – Informar próximos vencimientos (#195) | exists |
| Specification | `openspec/changes/tipo-documento-vencimiento-config/` | in progress |
| Branch | `feat/837_tipo-documento-vencimiento-config` | not created |
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
| Alta de tipo de documento que vence | `TipoDeDocumentoReferentialIntegrityTest#shouldCreateTipoDeDocumentoWithVencimiento` | pending |
| Alta de tipo de documento que no vence | `TipoDeDocumentoReferentialIntegrityTest#createTipoDocumento` (existing helper — already covers `vence=false` creation) | pending |
| Modificación de vencimiento y responsable | `TipoDeDocumentoReferentialIntegrityTest#shouldUpdateVencimientoAndQuienEntrega` | pending |
| Modificación bloqueada por tipo de documento en uso | `TipoDeDocumentoReferentialIntegrityTest#shouldReturn409WhenEditingTipoDocumentoInUse` (existing test — already covers this scenario) | pending |
| Alta de documento presentado de un tipo que vence | `DocumentoPresentadoControllerTest#shouldInheritVencimientoFromTipoDeDocumento` | pending |
| Alta de documento presentado de un tipo que no vence | `DocumentoPresentadoControllerTest#shouldNotSetVencimientoWhenTipoDoesNotVence` | pending |
| Cálculo de fecha de vencimiento | `DocumentoPresentadoControllerTest#shouldComputeFechaVencimientoFromFechaIngresoAndDiasVencimiento` | pending |
| Sin fecha de ingreso no hay fecha de vencimiento | `DocumentoPresentadoControllerTest#shouldNotComputeFechaVencimientoWithoutFechaIngreso` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU27 – Ingresar nuevo tipo de documento.md` | no | pending |
| `docs/100-business/102-use-cases/CU32 – Modificar tipo de documento.md` | no | pending |
| `docs/100-business/102-use-cases/CU42 – Informar próximos vencimientos.md` | no | pending |
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
