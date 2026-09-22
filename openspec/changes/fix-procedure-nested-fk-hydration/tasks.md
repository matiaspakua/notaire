> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #981 exists, labeled (`bug`, `BACKEND`, `TEST`), open
- [x] 1.2 Use Case CU82 documentation exists and is accurate (`docs/100-business/102-use-cases/CU82 – Generar Minuta de Inscripción.md`)
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/procedure-fk-hydration/spec.md`
- [x] 1.4 Impact Analysis confirmed in `proposal.md`
- [x] 1.5 No ADR required — follows existing `RegistrationDraftController` pattern, not a new architectural decision
- [x] 1.6 `gh issue edit 981 --add-label "in-progress"`

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b fix/981_procedure_nested_fk_hydration`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: real-Deed-id create (happy path), real-Deed-id update, missing `idProcedureType` (400), non-existent referenced id (404), GET-after-create reflects real state
- [ ] 3.2 Write `ProcedureControllerFkHydrationIntegrationTest` covering all 5 scenarios in the delta spec
- [ ] 3.3 (Integration-level covers this change; no separate unit-only cases beyond `SimpleControllersTest` rewrite in group 5)
- [ ] 3.4 Run `mvn test -pl backend-api -Dtest=ProcedureControllerFkHydrationIntegrationTest` — **must fail** (current code returns blank/default Deed state)
- [ ] 3.5 Confirm all 5 `#### Scenario:` entries in the delta spec map to a test method

## 4. Implementación

- [ ] 4.1 Add `ProcedureController.ProcedureRequest` record: `idProcedureType` (required), `idProperty`, `idDeed`, `idManagement`, `idBudget` (all optional), `notes`
- [ ] 4.2 Inject `PropertyRepositoryPort`, `BudgetRepositoryPort`, `ProcedureTypeRepositoryPort`, `DeedRepository`, `DeedManagementRepository` into `ProcedureController`
- [ ] 4.3 Rewrite `create(ProcedureRequest)`: resolve `idProcedureType` (400 if missing, 404 if not found), resolve each optional id if present (404 if not found), build `Procedure`, save, return `201`
- [ ] 4.4 Rewrite `update(Integer id, ProcedureRequest)` with the same resolution rules
- [ ] 4.5 Run the new test from group 3 — confirm it now passes

## 5. Actualizar tests existentes

- [ ] 5.1 Rewrite `SimpleControllersTest.ProcedureControllerTests`: mock the new dependencies, construct `ProcedureRequest` JSON instead of serializing a raw `Procedure` entity — old expectation (raw entity as request body) is now wrong because the contract legitimately changed
- [ ] 5.2 Update `ProcedureSerializationIntegrationTest.createProcedureForBudget` to the new flat-id request body (`{"notes": "...", "idProcedureType": N, "idBudget": N}`) — asserted behavior (no cyclic recursion) is unchanged, only the request shape
- [ ] 5.3 No tests expected to become obsolete

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — Checkstyle, SpotBugs
- [ ] 6.4 Update `backend-api/api-test/tramites/01-create.yml` and `04-update.yml` to flat-id bodies; run `bash testing/scripts/test.sh`
- [ ] 6.5 No `@Disabled` tests introduced

## 7. Ejecutar Playwright

- [ ] 7.1 Update `frontend/tests/e2e/setup/api-helpers.ts`'s `seedProcedure` and `TS-0082-minuta-inscripcion-feature.spec.ts`'s inline `apiPost` call to flat-id bodies
- [ ] 7.2 `cd frontend && npx playwright test TS-0082` — golden path and both edge cases green
- [ ] 7.3 n/a — no UI screens changed, viewport check not applicable (pure API contract fix)
- [ ] 7.4 Run the full Playwright suite once to confirm no other spec depended on the old nested-body shape via `seedProcedure`

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 No permanent documents identified requiring updates (see `proposal.md` — Documentation Impact); double-check during implementation for any doc referencing the old nested-body shape
- [ ] 8.2 Swagger/OpenAPI updates automatically via the new `ProcedureRequest` record — verify in Swagger UI after implementation
- [ ] 8.3 `CHANGELOG.md`: add `[Unreleased]` entry — this is a user-facing (API contract) bug fix
- [ ] 8.4 No documents to archive
- [ ] 8.5 Confirm no duplicated documentation was introduced
- [ ] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1 Commit 1: failing tests (`ProcedureControllerFkHydrationIntegrationTest`)
- [ ] 9.2 Commit 2: implementation (`ProcedureController` changes) — tests now pass
- [ ] 9.3 Commit 3: existing test updates (`SimpleControllersTest`, `ProcedureSerializationIntegrationTest`, Bruno, Playwright)
- [ ] 9.4 Final commit message ends with `Closes #981`; record all SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/981_procedure_nested_fk_hydration`
- [ ] 10.2 Open PR titled `[#981] fix(backend): resolve Procedure nested FK hydration on create/update`
- [ ] 10.3 Wait for `ci.yml`, `pr-validation.yml`, `playwright-e2e.yml` green
- [ ] 10.4 Gate 4 — CI green, code owner merge counts as review approval, no conflicts, docs complete
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm `cd.yml` published the image
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `POST /api/v1/tramites` with a real `idDeed` on the deployed environment, confirm the response embeds real Deed state
- [ ] 12.2 Confirm rollback path (plain revert, no data migration) is available
- [ ] 12.3 Close issue #981, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-procedure-nested-fk-hydration`

## Definition of Done

- [ ] Issue linked to CU82, with Acceptance Criteria in the delta spec
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E (`TS-0082`)
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green (`TS-0082` golden path + 2 edge cases)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing #981
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, issue #981 closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
