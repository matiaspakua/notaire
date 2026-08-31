# Tasks: Presupuesto persistence must keep its client association

**Input**: `plan.md`, `spec.md` from `speckit/specs/004-cu01-presupuesto-persona-fix/`

### Gate 1 — Prerequisites

- [x] Issue #883 created, linked to CU01 — Preparar Presupuesto, moved to `in-progress`.
- [x] `spec.md` written with Given/When/Then Acceptance Scenarios and Notaire Traceability.
- [x] `plan.md` written with Testing / Regression / Playwright / Deployment / Rollback Strategy.
- [x] `bash scripts/validate-speckit-plan.sh 004-cu01-presupuesto-persona-fix` passes (Gate 1).

### Crear branch

- [x] `git checkout main && git pull origin main`
- [x] `git checkout -b fix/883_presupuesto-persona-association`

### Tests for User Story phases (TDD)

- [x] Write `PresupuestoPersonaAssociationPgIntegrationTest` (`@Tag("pg-integration")`) covering US1 scenarios 1-3 and US2 scenarios 1-2 from `spec.md`. (`610d5ed`)
- [x] Run `mvn test -pl backend-api -Ppg-integration -Dtest=PresupuestoPersonaAssociationPgIntegrationTest` and confirm it **fails** against current `main` (proves the bug and that the test actually exercises it).

### Implementation for User Story phases

- [x] Add `@JsonProperty("persona")` to `Presupuesto.getFkIdPersona()` and `Presupuesto.setFkIdPersona()`, mirroring the existing `@JsonProperty("monto")` pattern on `montoInmueble`. (`946ad41`)
- [x] Re-run `PresupuestoPersonaAssociationPgIntegrationTest` and confirm it now **passes** (5/5 green).

### Actualizar tests existentes

- [x] Update `PresupuestoResumenControllerTest` request body: `fkIdPersona` → `persona`.
- [x] Update `GestionResumenFinancieroControllerTest` request body: `fkIdPersona` → `persona`.
- [x] Update `GestionArchiveIntegrationTest` request body: `fkIdPersona` → `persona`.
- [x] Update `GestionControllerIntegrationTest#createPresupuesto` helper body: `fkIdPersona` → `persona`.

### Ejecutar regresión

- [x] `mvn verify -pl backend-api` — full suite green (787 tests, 0 failures/errors), JaCoCo ratchet floor holds.
- [x] `mvn checkstyle:check -pl backend-api` and `mvn spotbugs:check -pl backend-api -DskipSpotBugs=false` clean (only pre-existing, unrelated findings).

### Ejecutar Playwright

- [x] Write `frontend/tests/e2e/cu01-presupuesto-persona.spec.ts` (create with cliente, verify persisted; search by apellido finds it). (`d934ced`)
- [x] `npx playwright test cu01-presupuesto-persona --project=chromium` green (2/2); full suite regression 319 passed / 37 skipped, no new failures.

### Gate 3 — Actualizar documentación permanente

- [x] Update `CHANGELOG.md` with the fix entry. (`fb2c52c`)
- [ ] Note the fix in CU01's use-case doc if it documents this contract — not applicable; CU01 doc describes user-facing behavior only, not the JSON wire contract.

### Commits atómicos

- [x] Commit 1: `test(presupuesto): add failing PG integration test for persona association` — `610d5ed`.
- [x] Commit 2: `fix(presupuesto): bind persona field from frontend request contract` (updates entity + the 4 existing tests together, since they're one atomic behavior-preserving contract fix) — `Closes #883` — `946ad41`.
- [x] Commit 3: `test(e2e): add CU01 Playwright coverage for presupuesto-persona association` — `d934ced`.
- [x] Commit 4: `docs(changelog): record presupuesto-persona association fix` — `fb2c52c`.

### Pull Request y validación CI

- [ ] `bash scripts/run_pipeline.sh` green.
- [ ] `git push -u origin fix/883_presupuesto-persona-association`.
- [ ] `gh pr create` referencing `Closes #883`.
- [ ] Wait for CI green on the PR.

### Deploy

- [ ] Merge PR (squash) once CI is green.
- [ ] `docker compose build backend && docker compose up -d backend` on the dev stack to pick up `main`.

### Gate 5 — Smoke test y cierre

- [ ] Smoke test against rebuilt `main`: `POST /api/v1/presupuestos` with `"persona":{"idPersona":<id>}` → `201`, `GET` the created id back and confirm `"persona"` is populated.
- [ ] `gh issue close 883` with a comment linking the merged PR and smoke-test evidence.
- [ ] Archive: move `speckit/specs/004-cu01-presupuesto-persona-fix/` to `speckit/specs/archive/004-cu01-presupuesto-persona-fix/`, with `tasks.md`/`traceability.md` updated to record final commit SHAs and CI/smoke-test evidence.

## Definition of Done

- [x] All TDD/implementation/regression/E2E/docs tasks above checked off.
- [x] `mvn verify -pl backend-api` and `mvn test -Ppg-integration` green.
- [x] Playwright spec green.
- [ ] PR merged, CI green, Issue #883 closed.
- [ ] Feature archived under `speckit/specs/archive/`.
