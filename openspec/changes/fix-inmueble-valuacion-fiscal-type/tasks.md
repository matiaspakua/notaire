Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
Workflow, §6 Quality Gates. Groups 1-12 mirror `scripts/validate-sdlc-plan.sh`;
only group 4 is change-specific — do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, linked to Use Case (CU69) — #879
- [x] 1.2 Use Case documentation exists and is accurate — CU69 – Gestión de
      Inmuebles
- [x] 1.3 Acceptance Criteria defined as scenarios in delta spec
- [x] 1.4 Impact Analysis of affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR recorded — n/a, not an architectural change
- [x] 1.6 Move Issue to IN PROGRESS

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/879_inmueble-valuacion-fiscal-type`
- [x] 2.3 Record branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: numeric value, null/omitted value (update
      scenario dropped — see Issue #880 and design.md)
- [x] 3.2 Write Postgres-backed integration tests for each scenario in the
      delta spec (`InmuebleValuacionFiscalPgIntegrationTest`)
- [x] 3.3 n/a — no separate unit test needed beyond the integration coverage
      (the bug only manifests at the JDBC binding layer)
- [x] 3.4 Run and observe them fail —
      `mvn test -Ppg-integration -Dtest=InmuebleValuacionFiscalPgIntegrationTest`
- [x] 3.5 Confirm each `#### Scenario:` in the delta spec maps to at least one
      test

## 4. Implementación

- [x] 4.1 Change `Inmueble.valuacionFiscal` (`negocio/Inmueble.java`) from
      `String` to `Float`, including getter/setter
- [x] 4.2 Change `DtoInmueble.valuacionFiscal`
      (`notaire-shared/.../dto/DtoInmueble.java`) from `String` to `Float`,
      including getter/setter/constructor
- [x] 4.3 Update `Inmueble.getDto()` / `setAtributos()` mapping if type
      coercion is needed
- [x] 4.4 Update `frontend/src/types/index.ts` — `Inmueble.valuacionFiscal`
      from `string` to `number`
- [x] 4.5 Update `frontend/src/app/dashboard/inmuebles/page.tsx` — form state
      and create/edit payload construction to convert the text input to a
      number (or `undefined` when empty)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected: `EntitiesBasicTest`,
      `InmuebleRepositoryIntegrationTest`, `InmuebleServiceIntegrationTest`,
      `TramiteEntityTest` (verified, no change needed)
- [x] 5.2 Update string literals/assertions to numeric ones without weakening
      coverage
- [x] 5.3 Remove any assertion made genuinely obsolete (e.g.
      `Integer.parseInt(...)` range checks replaced by direct `Float`
      comparisons), stating why

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — 1611/1611 passing
- [x] 6.2 `mvn jacoco:check -pl backend-api` — bound to `verify`, ratchet floor
      held
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle,
      SpotBugs) — BUILD SUCCESS
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite — passed
- [x] 6.5 No `@Disabled` or skipped tests without documented justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `cu69-inmuebles-valuacion-fiscal.spec.ts` under
      `frontend/tests/e2e/`
- [x] 7.2 `cd frontend && npx playwright test cu69-inmuebles-valuacion-fiscal`
      — 5/5 passing (rebuilt `notaire-backend` Docker image first, so the
      running stack picked up the fix)
- [x] 7.3 Verify the Inmueble form at 320px, 768px, 1024px
- [x] 7.4 n/a — this change has a UI surface (see 7.1)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update CU69 – Gestión de Inmuebles doc if it describes the field's
      data type — verified it doesn't (no field types documented), n/a
- [x] 8.2 n/a — no new/changed OpenAPI shape beyond the existing
      `valuacionFiscal` field's type (Swagger regenerates from the entity)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — bug fix + BREAKING API
      shape note
- [x] 8.4 n/a — no documents superseded
- [x] 8.5 Confirm no information duplicated
- [x] 8.6 `bash scripts/preflight.sh --fix` — 16 passed, 2 skipped (trivy not
      installed locally; Playwright/Bruno/Docker skipped without `--full`,
      already verified separately in groups 6-7)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends `Closes #879`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/879_inmueble-valuacion-fiscal-type`
- [ ] 10.2 Open PR titled
      `[#879] fix: align Inmueble.valuacionFiscal type with real column`
- [ ] 10.3 Wait for required workflows to pass
- [ ] 10.4 Address any CI failures
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Merge

- [ ] 11.1 Merge PR (squash), confirm `main` green
- [ ] 11.2 n/a — no CD/GHCR release tracked for this repo's dev flow beyond
      merge-to-main
- [ ] 11.3 Record merge commit SHA in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run smoke test: `POST /api/v1/inmueble` with a numeric
      `valuacionFiscal` against the running dev stack, confirm `201 Created`
- [ ] 12.2 Verify rollback path still available as described in `design.md`
- [ ] 12.3 Close GitHub Issue #879, referencing the PR
- [ ] 12.4 Archive change: `openspec archive fix-inmueble-valuacion-fiscal-type`

## Definition of Done

- [ ] Issue linked to Use Case, Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
