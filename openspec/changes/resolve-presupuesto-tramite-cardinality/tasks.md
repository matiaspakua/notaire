> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #798 exists, labeled, and linked to Use Cases CU01 – Preparar
      Presupuesto and CU45 – Modificar presupuesto
- [x] 1.2 Use Case documentation (CU01, CU45) already exists and is accurate — no
      update needed; this change only makes their existing cardinality explicit in code
- [x] 1.3 Acceptance Criteria defined as scenarios in
      `specs/presupuesto-tramite-relation/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR needed — targeted data-model correction, not a new architectural
      pattern (see `design.md` — Architecture review)
- [x] 1.6 Move Issue #798 to IN PROGRESS: `gh issue edit 798 --add-label "in-progress"`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/798_resolve-presupuesto-tramite-cardinality`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/presupuesto-tramite-relation/spec.md`:
      Tramite associates to Presupuesto; a Presupuesto holds many Tramites; a Tramite
      belongs to at most one Presupuesto; Presupuesto exposes no single-tramite field;
      migration refuses to drop the column if data would be lost
- [x] 3.2 Write `PresupuestoEntityTest` — replace the existing "Should link presupuesto
      to tramite" test (calls the soon-to-be-removed `setFkIdTramite`) with a test
      asserting two Tramites can be added to one Presupuesto's `tramiteList`, and a test
      asserting `Presupuesto`/`DtoPresupuesto` exposes no single-tramite accessor
- [x] 3.3 Write `TramiteEntityTest` — new test asserting reassigning a Tramite's
      `fkIdPresupuesto` from Presupuesto A to B removes it from A's `tramiteList`
- [x] 3.4 Extend `GestionControllerIntegrationTest` with a test asserting a Tramite
      created via the complete-case flow persists its Presupuesto reference and appears
      in that Presupuesto's `tramiteList`
- [x] 3.5 Write the `FlywaySchemaValidationIntegrationTest` guard test: seed a non-null
      `presupuestos.fk_id_tramite` row before migration `V14` runs and assert the
      migration fails without dropping the column
- [x] 3.6 Run all new/updated tests and **observe them fail**:
      `mvn test -pl backend-api -Dtest=PresupuestoEntityTest,TramiteEntityTest`
- [x] 3.7 Confirm every `#### Scenario:` in the delta spec maps to at least one test
      listed above

## 4. Implementación

- [x] 4.1 Remove `Presupuesto.fkIdTramite` field, getter, and setter
      (`negocio/Presupuesto.java`)
- [x] 4.2 Remove the `tramite`/`fkIdTramite` mapping from `Presupuesto.getDto()` and
      `setAtributos()`, and from `DtoPresupuesto`
- [x] 4.3 Remove the `fkIdTramite` read/write call sites in `ControllerNegocio.java`
      (~lines 740, 820, 977-984), reading the surrounding methods in full first to
      confirm no other logic shares those lines
- [x] 4.4 Remove the `fkIdTramite` read/write call sites in
      `PresupuestoJpaController.java` (~lines 64-67, 162-207, 345)
- [x] 4.5 Add `backend-api/src/main/resources/db/migration/V14__drop_presupuestos_fk_id_tramite.sql`:
      pre-drop check that `presupuestos.fk_id_tramite` has no non-null rows (fail loudly
      if it does), drop the `fk_presupuestos_tramite` constraint, drop the column
- [x] 4.6 Add the paired rollback script
      `R14__restore_presupuestos_fk_id_tramite.sql` (re-adds the column, nullable, no
      data), per `.claude/rules/database-migrations.md`
- [x] 4.7 Run `mvn test -Ppg-integration` to validate `V14` against a real PostgreSQL
      instance via `FlywaySchemaValidationIntegrationTest` and
      `FlywayMigrationScriptTest`

## 5. Actualizar tests existentes

- [x] 5.1 `grep -rn "FkIdTramite" backend-api/src/test` and update every remaining
      reference (see `design.md` — Regression Strategy)
- [x] 5.2 Update `unit/jpa/RemainingControllersJpaTest.java` and
      `unit/jpa/AdministradorJpaTest.java` if either exercises the removed
      `PresupuestoJpaController`/`ControllerNegocio` code paths
- [x] 5.3 Document in the commit body why the old "Should link presupuesto to tramite"
      expectation was wrong (it asserted the contradictory relation this change removes)
      rather than silently deleting it

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (~74% branch /
      ~84% line per `.claude/rules/code-quality.md`)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite, re-run the
      presupuestos collection to confirm the `tramite` field removal is the only shape
      change
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface. No frontend page reads or writes a single-tramite field
      on Presupuesto today (verified via grep of `frontend/src`); this change is
      entity/schema only (see `design.md` — Playwright Strategy)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/01-business/00-FUNCTIONAL-BASELINE.md` if it documents the
      Presupuesto↔Tramite relation, to state the resolved cardinality (one Presupuesto →
      many Tramites)
- [x] 8.2 No OpenAPI/Swagger annotation changes needed beyond the `DtoPresupuesto`
      shape change already reflected by removing the `tramite` field from its schema
- [x] 8.3 Add a `CHANGELOG.md` `[Unreleased]` entry noting the removed
      `Presupuesto.fkIdTramite`/`tramite` field is no longer part of the Presupuesto API
      shape (**BREAKING**)
- [x] 8.4 Archive superseded documents into `docs/archive/` if any covered the removed
      relation
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single
      source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #798`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/798_resolve-presupuesto-tramite-cardinality`
- [ ] 10.2 Open the PR titled
      `[#798] fix: resolve contradictory Presupuesto-Tramite cardinality`, referencing
      Issue #798 and Use Cases CU01/CU45
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`,
      `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test: create a gestión end-to-end via
      `POST /api/v1/gestiones/complete-case` (CU02 flow) and confirm the Tramite's
      Presupuesto association is retrievable via `GET /api/v1/presupuestos/{id}`
- [ ] 12.2 Verify the `R14` rollback script is available and matches `design.md` —
      Rollback Strategy
- [ ] 12.3 Close GitHub Issue #798, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive resolve-presupuesto-tramite-cardinality`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a here — no UI surface)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
