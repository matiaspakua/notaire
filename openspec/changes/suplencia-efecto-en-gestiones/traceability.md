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
| Issue | #836 | open |
| Use Case | CU22 – Registrar Suplencia (#175); CU59 – Consultar Suplencias (#212); CU48 – Dar alta escribano (#201); CU51 – Modificar escribano (#204) | exists |
| Specification | `openspec/changes/suplencia-efecto-en-gestiones/` | in progress |
| Branch | `feat/836_suplencia-efecto-en-gestiones` | not created |
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
| Creación de gestión sin suplencia activa | `GestionSuplenciaServiceTest#shouldAssignRequestedEscribanoWhenNoActiveSuplencia` | pending |
| Creación de gestión con suplencia activa | `GestionSuplenciaServiceTest#shouldAssignSuplenteWhenEscribanoHasActiveSuplencia` | pending |
| Edición de gestión con suplencia activa | `GestionControllerIntegrationTest#shouldRedirectToSuplenteWhenUpdatingGestionEscribano` | pending |
| Observaciones registran el redireccionamiento | `GestionSuplenciaServiceTest#shouldRecordRedirectionInObservaciones` | pending |
| Alta de registro de escribano | `PersonaServiceTest#shouldRegisterEscribanoCredentialOnExistingPersona` | pending |
| Modificación de registro de escribano | `PersonaServiceTest#shouldUpdateEscribanoCredentialOnExistingPersona` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | no | pending |
| `docs/100-business/102-use-cases/CU48 – Dar alta escribano.md` | no | pending |
| `docs/100-business/102-use-cases/CU51 – Modificar escribano.md` | no | pending |
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
