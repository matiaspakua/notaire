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
- [ ] 3.5 Write `ConceptoDeleteCascadeIntegrationTest` and `PresupuestoDeleteCascadeIntegrationTest` (integration, covering the two `EAGER`+`CascadeType.ALL` risk cases) — run and observe **FAIL**

## 4. Implementación

Batch 1 — catálogo/base tables:
- [x] 4.1 Implement `Persistable<Integer>` on `Rol`, `TipoDeDocumento`, `TipoDeFolio`, `TipoDeTramite`, `TipoIdentificacion`, `EstadoDeGestion`, `WorkflowDefinition`, `WorkflowNode`, `WorkflowTransition`
- [x] 4.2 Re-run the Batch-1 parameterized cases in `PersistableIdentityEntitiesIsNewTest` / `PersistableIdentityEntitiesDeleteIntegrationTest` and confirm they pass

Batch 2 — core domain entities:
- [ ] 4.3 Implement `Persistable<Integer>` on `Persona`, `Usuario`, `Escritura`, `GestionDeEscritura`, `Presupuesto`, `Testimonio`, `Cuaderno`, `Folio`, `Inmueble`, `MinutaInscripcion`, `MovimientoTestimonio`, `DocumentoPresentado`, `RegistroAuditoria`, `Suplencia`, `Concepto`, `Copia`
- [ ] 4.4 Re-run the Batch-2 parameterized cases and confirm they pass

Batch 3 — `@EmbeddedId` composite-key entities:
- [ ] 4.5 Implement `Persistable<XxxPK>` with the `@Transient boolean isNew` + `@PostLoad`/`@PrePersist` pattern (design.md Decision 2) on `FoliosCopias`, `PlantillaCostoDocumento`, `PlantillaPresupuesto`, `PlantillaTramite`, `TramitesPersonas`
- [ ] 4.6 Re-run `PersistableEmbeddedIdEntitiesIsNewTest` / `PersistableEmbeddedIdEntitiesDeleteIntegrationTest` and confirm they pass

Batch 4 — cascade risk cases:
- [ ] 4.7 Run `ConceptoDeleteCascadeIntegrationTest` and `PresupuestoDeleteCascadeIntegrationTest` against the now-fixed `Concepto`/`Presupuesto` and confirm both parent and cascaded children rows are absent after delete
- [ ] 4.8 If either test still fails after the `Persistable` fix (i.e. the `EAGER`+`ALL` cascade itself, not `isNew()`, is reverting the delete), file a new Issue for the cascade behavior per proposal.md's Out of Scope and record it in `openspec/explore.md` — do not patch it inline in this change

## 5. Actualizar tests existentes

- [ ] 5.1 Run the existing suites that exercise these 30 entities' `DELETE` endpoints (e.g. `backend-api/api-test/folios/`, `backend-api/api-test/suplencias/`) and confirm none needed an assertion change — a previously-passing "200 but not deleted" test is not expected to exist, since no prior test asserted the row was actually gone
- [ ] 5.2 Confirm `HistorialDeleteIntegrationTest` (the reference template, `Historial`/`Item`/`Pago`/`Tramite`) is untouched and still green

## 6. Ejecutar regresión

- [ ] 6.1 `mvn verify -pl backend-api`
- [ ] 6.2 `mvn jacoco:check -pl backend-api` (confirm ratchet floor 70% line / 25% branch still held)
- [ ] 6.3 `bash testing/scripts/test.sh` (Bruno HTTP suite against a running API)

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface (design.md Playwright Strategy). Existing Playwright suite (`cd frontend && npx playwright test`) is still run once as part of `scripts/run_pipeline.sh` to confirm no incidental frontend regression from the backend delete-path fix.

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `backend-api/api-test/COVERAGE.md` — add a defect entry (style of defecto #8/#9) listing the 30 entities fixed by this change
- [ ] 8.2 Update `CHANGELOG.md` — `[Unreleased]` entry for the silent-delete fix extension
- [ ] 8.3 Update `openspec/explore.md` — move the #957 row from "Pendientes" to "Resueltos"
- [ ] 8.4 Update `traceability.md` — mark Gate log entries, requirement coverage, and permanent-documentation rows as done with their commit references

## 9. Commits atómicos

- [ ] 9.1 Commit test scaffolding (group 3) — `test(negocio): add failing Persistable isNew/delete tests for remaining entities` — `Refs #957`
- [ ] 9.2 Commit Batch 1 (group 4.1–4.2) — `Refs #957`
- [ ] 9.3 Commit Batch 2 (group 4.3–4.4) — `Refs #957`
- [ ] 9.4 Commit Batch 3 (group 4.5–4.6) — `Refs #957`
- [ ] 9.5 Commit Batch 4 / cascade tests (group 4.7–4.8) — `Refs #957`
- [ ] 9.6 Commit documentation updates (group 8) — `docs(negocio): document silent-delete-persistable-fix — Closes #957`

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
