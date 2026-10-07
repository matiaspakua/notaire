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
| Issue | #839 | closed by this PR's merge — shared umbrella Issue covering 5 sub-changes (RF-74 a RF-95); remains open until `protocolo-numeracion-escrituras`, the last of the 5, also merges |
| Use Case | CU82 – Generar Minuta de Inscripción | exists (#313) |
| Specification | `openspec/changes/protocolo-minuta-inscripcion/` | complete |
| Branch | `feat/839_protocolo-minuta-inscripcion` | created |
| Tasks | `tasks.md` | complete |
| Commits | `7a6ab496` (feat), `cdbabec0` (docs), `0ff25e9e` (merge main) | complete |
| Pull Request | [#938](https://github.com/matiaspakua/notaire/pull/938) | merged |
| CI run | https://github.com/matiaspakua/notaire/actions/runs/33881397387 | passed |
| Merge commit | `2daa8625` | complete |
| Release / tag | Continuous deploy on merge to `main` | complete |
| Smoke test | `mvn verify` + Playwright E2E in CI, PR #938 | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Cargar datos registrales de un inmueble | `InmuebleControllerTest#shouldSaveMatriculaTomoFolioFincaYLinderos` | passing |
| Generar minuta con datos completos | `MinutaInscripcionControllerTest#shouldGenerateMinutaWhenDataIsComplete` | passing |
| Intento de generar minuta con datos incompletos | `MinutaInscripcionControllerTest#shouldRejectGenerationWhenDataIsIncomplete` | passing |
| Imprimir la minuta en formulario normalizado | `ReporteControllerTest#shouldGenerateMinutaInscripcionReport` | passing |
| Registrar presentación | `MinutaInscripcionControllerTest#shouldRegisterPresentacion` | passing |
| Registrar observación | `MinutaInscripcionControllerTest#shouldRegisterObservacion` | passing |
| Registrar inscripción definitiva | `MinutaInscripcionControllerTest#shouldRegisterInscripcionDefinitiva` | passing |
| E2E: golden path + edge paths (CU82) | `frontend/tests/e2e/TS-0082-minuta-inscripcion-feature.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md` | no — already accurate (GitHub ID #313 present) | — |
| `CHANGELOG.md` | yes | `cdbabec0` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `scripts/validate-sdlc-plan.sh` — protocolo-minuta-inscripcion ✓ |
| 2 | Failing tests written, test cases designed | yes | tests written first, observed failing, then implementation added |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` green; `bash scripts/preflight.sh --fix` all green (own change) |
| 4 | CI green, review approved, no conflicts | yes | PR #938 merged via `2daa8625` |
| 5 | Deployed, smoke test passed, Issue closed | yes | Continuous deploy on merge to `main`; Issue #839 closed (see note above — remains open pending the last of 5 sub-changes) |

## Exceptions

None.
