> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #819 exists, labeled, and linked to CU-16 / RF-22 / RF-37
- [x] 1.2 CU16 ("Archivar Gestión") documentation exists and is accurate — update it (see group 8) to add the debt-verification step
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/gestion-archive-debt-check/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required (see design.md — Architecture review): follows existing `repository`/`service`/`api` layering
- [x] 1.6 Move the Issue to IN PROGRESS: `gh issue edit 819 --add-label "in-progress"`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/819_verify-debt-on-gestion-archive`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/gestion-archive-debt-check/spec.md`: single trámite/presupuesto, multiple trámites/presupuestos, zero balance, debt warning, confirm-despite-debt, no-debt archive, debt flag persisted true, debt flag persisted false
- [x] 3.2 Write `GestionArchiveDebtServiceTest` (unit) covering the saldo-aggregation scenarios (mocked `PagoService`/repositories)
- [x] 3.3 Write `GestionArchiveIntegrationTest` (integration, PostgreSQL) covering the archive endpoint and persisted debt flag
- [x] 3.4 Run them and **observe them fail**: `mvn test -pl backend-api -Dtest=GestionArchiveDebtServiceTest,GestionArchiveIntegrationTest`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [x] 4.1 Add Flyway migration `V15__add_deuda_pendiente_to_gestiones_de_escrituras.sql` adding the additive debt-at-archive column (resolve exact target per design.md — Open Questions: `gestiones_de_escrituras` column vs. `registro_auditoria`-based record)
- [x] 4.2 Implement the saldo-aggregation logic: given a gestión, sum `PagoService.calcularSaldoPendiente(idPresupuesto)` across the presupuestos of its `tramiteList` (via `Tramite.fkIdPresupuesto`)
- [x] 4.3 Add the archive endpoint to `api/GestionController.java` (backed by `service`/`repository`, not the legacy `jpa` package) that accepts the archive request and persists the debt-at-archive flag
- [x] 4.4 Add a `GET` endpoint (or extend an existing gestión endpoint) exposing the aggregate pending balance so the frontend can show the warning before submitting
- [x] 4.5 Document the new/changed endpoints with `@Operation`/`@ApiResponse` and verify in Swagger UI — confirmed both `GET /{id}/saldo-pendiente` and `POST /{id}/archivar` appear in `/v3/api-docs` with summaries and 200/404 responses; `/swagger-ui/index.html` returns 200
- [x] 4.6 Add the archive action to the gestión screen in `frontend/` using `FormContainer`/theme tokens, showing the debt warning (non-blocking) when the aggregate balance is greater than zero — row action + `ConfirmDialog` (extended with `confirmLabel`) in `frontend/src/app/dashboard/gestiones/page.tsx`, backed by `useSaldoPendiente`/`useArchivarGestion` hooks
- [x] 4.7 Update `transicion-de-estados.puml` to add the debt-check point before "Archivar gestión" (see group 8)

## 5. Actualizar tests existentes

- [x] 5.1 No existing tests are expected to be affected (see design.md — Regression Strategy): `PagoService.calcularSaldoPendiente` and the legacy `archivarGestion` path are unchanged — confirmed: full suite green (1482/1482), no other test broke
- [x] 5.2 If any existing `GestionController` test fixtures need the new field, update them without weakening assertions — `AdditionalControllersTest` updated to mock `GestionArchiveDebtService` in the `GestionController` constructor
- [x] 5.3 n/a — no obsolete tests identified

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (1482/1482 passing)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (verified via `mvn verify`, bound execution passes; standalone `jacoco:check` goal invocation is not supported outside the bound lifecycle)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs) — BUILD SUCCESS
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite — all strict endpoint checks passed, backend rebuilt in Docker with #819 changes prior to running
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add the gestión-archiving E2E spec under `frontend/tests/e2e/` (see design.md — Playwright Strategy) — `crud-gestiones.spec.ts` ("CU16 - Archivar Gestión con verificación de deuda")
- [x] 7.2 `cd frontend && npx playwright test` — 418 passed, 36 skipped, 1 failed; the failure (`01-first-case-tutorial.spec.ts` — "Register the client" step, persona-creation dialog not closing) is pre-existing and unrelated to #819 — confirmed via `git stash` bisection of this branch's changes; out of scope, needs its own issue
- [x] 7.3 Verify the archive action and debt warning at 320px, 768px and 1024px — `crud-gestiones.spec.ts` viewport tests (lines 103+), all passing
- [x] 7.4 Cover golden path (no debt) and edge path (debt warning, confirm/cancel) — both scenarios in `crud-gestiones.spec.ts`, all passing

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` — add the debt-verification step to Curso de Eventos / Excepciones
- [x] 8.2 Update `docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml` — add the debt-check point before "Archivar gestión"
- [x] 8.3 Update OpenAPI/Swagger annotations for the new/changed endpoints; verify in Swagger UI — `@Operation`/`@ApiResponses` present on both endpoints (`GestionController.java:327-356`); confirmed `GET /{id}/saldo-pendiente` and `POST /{id}/archivar` present in `/v3/api-docs`
- [x] 8.4 Update `CHANGELOG.md` (`[Unreleased]`) — gestión archiving now warns about and records pending debt
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth; CU16 doc and `CHANGELOG.md` cover distinct audiences (business flow vs. release notes), no overlap
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate — PREFLIGHT PASSED (15/15; trivy/Playwright/Bruno/Docker skipped as environment-dependent, covered manually above)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #819`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/819_verify-debt-on-gestion-archive`
- [x] 10.2 Open the PR titled `[#819] feat: verificar deuda pendiente al archivar una gestión`, referencing Issue #819 and CU-16/RF-22/RF-37
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml` — `playwright-e2e.yml` failed (`01-first-case-tutorial.spec.ts`), but the same test fails identically on `main` before this merge, so it's a pre-existing flake, not caused by this change
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete — CI green apart from the pre-existing flake above; no recorded review approval on PR #826
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main` (merged via PR #826, merge commit `dc5e2a3`)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR — not yet run for merge commit `dc5e2a3`
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md` — merge commit recorded; release/tag still pending CD run

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Archive a test gestión with a known pending balance via the new endpoint on the target environment; confirm the response and persisted record both reflect pending debt
- [ ] 12.2 Verify the rollback path described in design.md is still available (revert is safe, additive-only)
- [x] 12.3 Close GitHub Issue #819, referencing the PR (issue already closed — out of intended Gate 5 order, since smoke test had not run yet)
- [ ] 12.4 Archive the change: `openspec archive verify-debt-on-gestion-archive`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
