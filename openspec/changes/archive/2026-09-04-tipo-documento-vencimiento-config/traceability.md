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
| Issue | #837 | closed |
| Use Case | CU27 – Ingresar nuevo tipo de documento (#180); CU32 – Modificar tipo de documento (#185); CU42 – Informar próximos vencimientos (#195) | exists |
| Specification | `openspec/changes/tipo-documento-vencimiento-config/` | complete |
| Branch | `feat/837_tipo-documento-vencimiento-config` | merged |
| Tasks | `tasks.md` | see below |
| Commits | `86e641f0`, `1dd6a7ad` | done |
| Pull Request | #935 | merged |
| CI run | PR #935 checks | green |
| Merge commit | `3daf8759` | done |
| Release / tag | — | not applicable (continuous deploy, no tagged release) |
| Smoke test | admin smoke 22/22 passing (per Gate 3 evidence) | done |

## Requirement coverage

The alta/edición coverage originally planned as backend unit tests on
`TipoDeDocumentoReferentialIntegrityTest` was implemented instead as
Playwright E2E specs against the real admin form (CU27/CU32 are UI-driven
scenarios; `TipoDeDocumentoController` already persisted `vence`/
`diasVencimiento`/`quienEntrega` via the existing `DtoTipoDeDocumento`, so
there was no new backend branching to unit-test there — the gap was the
missing UI form and the herencia logic in `DocumentoPresentadoController`).

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta de tipo de documento que vence, campo días visible | `tipo-documento-vencimiento-config.spec.ts#CU27-GW01` | passing |
| Validación: vence sin días de vencimiento | `tipo-documento-vencimiento-config.spec.ts#CU27-GW02` | passing |
| Alta de tipo de documento con vencimiento completo, persistido | `tipo-documento-vencimiento-config.spec.ts#CU27-GW03` | passing |
| Edición: campos de vencimiento pre-cargados | `tipo-documento-vencimiento-config.spec.ts#CU32-GW01` | passing |
| Alta de documento presentado de un tipo que vence | `DocumentoPresentadoControllerTest#shouldInheritVencimientoFieldsFromTipoDeDocumento` | passing |
| Alta de documento presentado de un tipo que no vence | `DocumentoPresentadoControllerTest#shouldNotComputeFechaVencimientoWhenTipoDoesNotVence` | passing |
| Un `quienEntrega` explícito prevalece sobre el heredado del tipo | `DocumentoPresentadoControllerTest#shouldLetExplicitQuienEntregaOverrideTipoDeDocumento` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU27 – Ingresar nuevo tipo de documento.md` | no — already accurate | — |
| `docs/100-business/102-use-cases/CU32 – Modificar tipo de documento.md` | no — already accurate | — |
| `docs/100-business/102-use-cases/CU42 – Informar próximos vencimientos.md` | yes — added herencia note | `1dd6a7ad` |
| `CHANGELOG.md` | yes | `86e641f0` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #837, `proposal.md`, `specs/` |
| 2 | Failing tests written, test cases designed | yes | E2E specs + `DocumentoPresentadoControllerTest` written before implementation |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` exit 0; Playwright 4/4 + admin smoke 22/22 passing |
| 4 | CI green, review approved, no conflicts | yes | PR #935 merged via `3daf8759` |
| 5 | Deployed, smoke test passed, Issue closed | yes | Continuous deploy on merge to `main`; Issue #837 closed |

## Exceptions

None.
