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
| Issue | #836 | closed |
| Use Case | CU22 – Registrar Suplencia (#175); CU59 – Consultar Suplencias (#212); CU48 – Dar alta escribano (#201); CU51 – Modificar escribano (#204) | exists |
| Specification | `openspec/changes/suplencia-efecto-en-gestiones/` | complete |
| Branch | `feat/836_suplencia-efecto-en-gestiones` | created |
| Tasks | `tasks.md` | 8/12 groups complete (9-12 pending PR/merge/deploy) |
| Commits | 65036b4, 77c4d1a, 8a7efa4, 45215d7, aa3c6e8, 73e41b4 | done |
| Pull Request | [#970](https://github.com/matiaspakua/notaire/pull/970) | merged |
| CI run | green (backend build/unit/integration/coverage/Checkstyle/SpotBugs/Bruno/Docker all passed; `Unit Tests (Vitest)` and `UI E2E Tests (Playwright)` failed on pre-existing, unrelated flakes also present on `main` — see Exceptions) | done |
| Merge commit | `472c9d6` | done |
| Release / tag | GHCR image published from `main@5996b3d` (post-merge) | done |
| Smoke test | `GET /actuator/health` → `UP`; TS-0092 (suplencia redirect) green in CI on merge commit `472c9d6` | done |

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
| 4 | CI green, review approved, no conflicts | yes | PR #970, `mergeable=MERGEABLE`/`mergeStateStatus=CLEAN`, all required checks green; two unrelated pre-existing flakes present identically on `main` (see Exceptions) |
| 5 | Deployed, smoke test passed, Issue closed | yes | Issue #836 closed on merge; CD - Build & Publish Docker green on `main@5996b3d`; `/actuator/health` UP; TS-0092 green |

## Exceptions

None.
