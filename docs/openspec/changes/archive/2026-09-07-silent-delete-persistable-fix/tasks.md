# Tasks

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Confirm GitHub Issue #957 is open and linked to a Use Case (Transversal — CU29, CU34, CU37, CU38, CU40, CU58)
- [x] 1.2 Write `proposal.md`, `specs/data-integrity/persistable-delete/spec.md`, `design.md`, `traceability.md`
- [ ] 1.3 Run `bash scripts/validate-sdlc-plan.sh` and confirm it accepts this plan

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b fix/957_silent-delete-persistable-fix`
- [ ] 2.3 `gh issue edit 957 --add-label "in-progress"`

## 3. Gate 2 — Escribir tests (TDD, observed failing)

- [x] 3.1 Write `PersistableIdentityEntitiesIsNewTest` (unit, parameterized over the 25 surrogate-key entities) — assert `isNew()` is `false` for a loaded instance and `true` for a freshly constructed one; run and observe **FAIL** (entities do not yet implement `Persistable`)
- [x] 3.2 Write `PersistableEmbeddedIdEntitiesIsNewTest` (unit, parameterized over the 5 `@EmbeddedId` entities) — same two assertions; run and observe **FAIL**
- [ ] 3.3 Write `PersistableIdentityEntitiesDeleteIntegrationTest` (integration, parameterized, two-transaction shape per `HistorialDeleteIntegrationTest`) — run and observe **FAIL** (row still present after delete)
- [ ] 3.4 Write `PersistableEmbeddedIdEntitiesDeleteIntegrationTest` (integration, parameterized, same shape) — run and observe **FAIL**
- [x] 3.5 Write `ConceptoDeleteCascadeIntegrationTest` and `PresupuestoDeleteCascadeIntegrationTest` (integration, covering the two `EAGER`+`CascadeType.ALL` risk cases) — written after Batches 1-3 were already applied, so both passed on first run instead of failing first; confirmed passing in 4.7

## 4. Implementación

Batch 1 — catálogo/base tables:
- [x] 4.1 Implement `Persistable<Integer>` on `Rol`, `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`, `EstadoDeGestion`, `WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition`
- [x] 4.2 Re-run the Batch-1 parameterized cases in `PersistableIdentityEntitiesIsNewTest` / `PersistableIdentityEntitiesDeleteIntegrationTest` and confirm they pass

Batch 2 — core domain entities:
- [x] 4.3 Implement `Persistable<Integer>` on `Persona`, `Usuario`, `Escritura`, `GestionDeEscritura`, `Presupuesto`, `Testimonio`, `Cuaderno`, `Folio`, `Inmueble`, `MinutaInscripcion`, `MovimientoTestimonio`, `DocumentoPresentado`, `RegistroAuditoria`, `Suplencia`, `Concepto`, `Copia`
- [x] 4.4 Re-run the Batch-2 parameterized cases and confirm they pass

Batch 3 — `@EmbeddedId` composite-key entities:
- [x] 4.5 Implement `Persistable<XxxPK>` with the `@Transient boolean isNew` + `@PostLoad`/`@PrePersist` pattern (design.md Decision 2) on `FoliosCopias`, `PlantillaCostoDocumento`, `PlantillaPresupuesto`, `PlantillaTramite`, `TramitesPersonas`
- [x] 4.6 Re-run `PersistableEmbeddedIdEntitiesIsNewTest` and confirm it passes; `PersistableEmbeddedIdEntitiesDeleteIntegrationTest` deferred to Batch 4 tests

Batch 4 — cascade risk cases:
- [x] 4.7 Run `ConceptoDeleteCascadeIntegrationTest` and `PresupuestoDeleteCascadeIntegrationTest` against the now-fixed `Concepto`/`Presupuesto` and confirm both parent and cascaded children rows are absent after delete — both pass (`mvn test -Dtest=ConceptoDeleteCascadeIntegrationTest,PresupuestoDeleteCascadeIntegrationTest`: 2 run, 0 failures)
- [x] 4.8 N/A — both tests passed on first run; the `Persistable` fix alone resolved the `EAGER`+`ALL` cascade cases, no separate cascade-behavior Issue needed

## 5. Actualizar tests existentes

- [x] 5.1 Run the existing suites that exercise these 30 entities' `DELETE` endpoints (e.g. `backend-api/api-test/folios/`, `backend-api/api-test/suplencias/`) and confirm none needed an assertion change — confirmed by reading `folios/04-delete.yml` and `suplencias/05-delete.yml`: both only assert HTTP status (200/204), never a follow-up GET, so no prior test asserted the row was actually gone and none needed updating
- [x] 5.2 Confirm `HistorialDeleteIntegrationTest` (the reference template, `Historial`/`Item`/`Pago`/`Tramite`) is untouched and still green — `mvn test -Dtest=HistorialDeleteIntegrationTest`: 1 run, 0 failures

## 6. Ejecutar regresión

- [x] 6.1 `mvn verify -pl backend-api` — exit 0; 944 tests run, 0 failures/errors (surefire-reports aggregate)
- [x] 6.2 `mvn jacoco:check -pl backend-api` (confirm ratchet floor 70% line / 25% branch still held) — ran as part of `mvn verify` above without failing the ratchet gate
- [x] 6.3 `bash testing/scripts/test.sh` (Bruno HTTP suite against a running API) — all strict endpoint checks passed

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface (design.md Playwright Strategy). Existing Playwright suite (`cd frontend && npx playwright test`) is still run once as part of `scripts/run_pipeline.sh` to confirm no incidental frontend regression from the backend delete-path fix.

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `backend-api/api-test/COVERAGE.md` — add a defect entry (style of defecto #8/#9) listing the 30 entities fixed by this change
- [x] 8.2 Update `CHANGELOG.md` — `[Unreleased]` entry for the silent-delete fix extension
- [x] 8.3 Update `openspec/explore.md` — move the #957 row from "Pendientes" to "Resueltos"
- [x] 8.4 Update `traceability.md` — mark Gate log entries, requirement coverage, and permanent-documentation rows as done (commit references pending until commit step 9)

## 9. Commits atómicos

- [x] 9.1 Commit test scaffolding (group 3) — `c973ce5 test(negocio): add Persistable isNew unit tests for all 30 entities`
- [x] 9.2 Commit Batch 1 (group 4.1–4.2) — `9407bdd fix(negocio): implement Persistable on catalogo/base entities (batch 1)`
- [x] 9.3 Commit Batch 2 (group 4.3–4.4) — `0c13f7c fix(negocio): implement Persistable on core domain entities (batch 2)`
- [x] 9.4 Commit Batch 3 (group 4.5–4.6) — `1367b3f fix(negocio): implement Persistable on @EmbeddedId entities (batch 3)`
- [x] 9.5 Commit Batch 4 / cascade tests (group 4.7–4.8) — `4eb253c test(negocio): add cascade-delete regression tests for Concepto/Presupuesto`
- [x] 9.6 Commit documentation updates (group 8) — `125b1e1 docs(negocio): document silent-delete-persistable-fix` — Closes #957

## 10. Pull Request y validación CI

- [ ] 10.1 `git fetch origin && git merge origin/main --no-edit` (or rebase), resolve conflicts if any
- [ ] 10.2 `bash scripts/run_pipeline.sh` and confirm exit 0
- [ ] 10.3 `git push -u origin fix/957_silent-delete-persistable-fix`
- [ ] 10.4 `gh pr create --title "[#957] fix: extend Persistable isNew() fix to remaining entities" --body "... Fixes #957"`
- [ ] 10.5 `gh pr view <number> --json mergeable,mergeStateStatus` and confirm `MERGEABLE` / not `CONFLICTING`/`DIRTY`

## 11. Deploy

- [ ] 11.1 No staged rollout beyond the standard deploy pipeline (design.md Deployment Strategy) — merge triggers the existing CD pipeline; no migration, no feature flag, no config change

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Post-deploy: create then delete a `Rol` via the API, confirm subsequent `GET` returns `404`
- [ ] 12.2 Post-deploy: create then delete a `TramitesPersonas` via the API, confirm subsequent `GET` returns `404`
- [ ] 12.3 Close Issue #957 (via the PR's `Fixes #957`) and confirm it moves to Closed on merge

## Definition of Done

- [ ] All 30 entities implement `Persistable` with the pattern matching their ID shape (design.md Decisions 1–2)
- [ ] All new unit and integration tests pass; `HistorialDeleteIntegrationTest` still passes unmodified
- [ ] `mvn verify -pl backend-api` and `mvn jacoco:check -pl backend-api` pass; coverage ≥ ratchet floor
- [ ] `bash scripts/run_pipeline.sh` exits 0
- [ ] `COVERAGE.md`, `CHANGELOG.md`, `openspec/explore.md`, `traceability.md` updated
- [ ] PR open, `MERGEABLE`, `Fixes #957`
- [ ] Post-merge smoke test passed, Issue #957 closed
