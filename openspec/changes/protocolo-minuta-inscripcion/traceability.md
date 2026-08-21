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
| Issue | #839 | open |
| Use Case | CU82 – Generar Minuta de Inscripción | exists (#313) |
| Specification | `openspec/changes/protocolo-minuta-inscripcion/` | in progress |
| Branch | `feat/839_protocolo-minuta-inscripcion` | pending |
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
| Cargar datos registrales de un inmueble | `InmuebleControllerTest#shouldSaveMatriculaTomoFolioFincaYLinderos` | pending |
| Generar minuta con datos completos | `MinutaInscripcionControllerTest#shouldGenerateMinutaWhenDataIsComplete` | pending |
| Intento de generar minuta con datos incompletos | `MinutaInscripcionControllerTest#shouldRejectGenerationWhenDataIsIncomplete` | pending |
| Imprimir la minuta en formulario normalizado | `ReporteControllerTest#shouldGenerateMinutaInscripcionReport` | pending |
| Registrar presentación | `MinutaInscripcionControllerTest#shouldRegisterPresentacion` | pending |
| Registrar observación | `MinutaInscripcionControllerTest#shouldRegisterObservacion` | pending |
| Registrar inscripción definitiva | `MinutaInscripcionControllerTest#shouldRegisterInscripcionDefinitiva` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` | no | — |
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
