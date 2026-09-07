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
| Issue | #836 | in-progress |
| Use Case | CU22 – Registrar Suplencia (#175); CU59 – Consultar Suplencias (#212); CU48 – Dar alta escribano (#201); CU51 – Modificar escribano (#204) | exists |
| Specification | `openspec/changes/suplencia-efecto-en-gestiones/` | complete |
| Branch | `feat/836_suplencia-efecto-en-gestiones` | created |
| Tasks | `tasks.md` | 8/12 groups complete (9-12 pending PR/merge/deploy) |
| Commits | 65036b4, 77c4d1a, 8a7efa4, 45215d7, aa3c6e8, 73e41b4 | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Creación de gestión sin suplencia activa | `GestionSuplenciaServiceTest#shouldAssignRequestedEscribanoWhenNoActiveSuplencia` | passing |
| Creación de gestión con suplencia activa | `GestionSuplenciaServiceTest#shouldAssignSuplenteWhenEscribanoHasActiveSuplencia` | passing |
| Edición de gestión con suplencia activa | `GestionControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingGestionEscribano` | passing |
| Observaciones registran el redireccionamiento | `GestionSuplenciaServiceTest#shouldRecordRedirectionInObservaciones` | passing |
| Alta de registro de escribano | `PersonaServiceTest#shouldRegisterEscribanoCredentialOnExistingPersona` | passing |
| Modificación de registro de escribano | `PersonaServiceTest#shouldUpdateEscribanoCredentialOnExistingPersona` | passing |
| E2E: alta/edición registro escribano y redirección de gestión | `TS-0092-gestion-suplencia-redirect.spec.ts` (4 tests) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | yes | 73e41b4 |
| `docs/100-business/102-use-cases/CU48 – Dar alta escribano.md` | yes | 73e41b4 |
| `docs/100-business/102-use-cases/CU51 – Modificar escribano.md` | n/a — no changes needed, covered by CU48 | — |
| `CHANGELOG.md` | yes | 73e41b4 |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #836, `proposal.md`, delta specs |
| 2 | Failing tests written, test cases designed | yes | `GestionSuplenciaServiceTest`, `PersonaServiceTest`, `GestionControllerIntegrationTest`, TS-0092 (observed failing before implementation) |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api` BUILD SUCCESS (1804 tests, 0 failures); `npx playwright test TS-0092` 4/4 passing; `bash testing/scripts/test.sh` passing; CU22/CU48/CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
