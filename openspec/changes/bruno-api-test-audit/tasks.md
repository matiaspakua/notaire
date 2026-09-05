> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (#952, CU76)
- [x] 1.2 Use Case documentation exists and is accurate (CU76)
- [x] 1.3 Acceptance Criteria defined (Issue #952 body)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural — no ADR required
- [x] 1.6 Issue already labeled `in-progress`

## 2. Crear branch

- [x] 2.1 `main` was up to date when work started
- [x] 2.2 Branch `test/952_bruno-api-test-audit` already created and active
- [x] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Test cases enumerated: CRUD lifecycle happy path per domain, budget-FK
      persistence (previously silently broken), balance/payment happy path
- [x] 3.2 N/A — no new unit tests (see design.md Testing Strategy: fix is a
      Jackson visibility annotation, proven at the API-contract level)
- [x] 3.3 N/A — no new integration tests (no schema/repository change)
- [x] 3.4 Reproduced the bug first via manual `curl` + DB inspection before
      fixing (`fk_id_presupuesto` confirmed `NULL` after `POST /api/v1/items`
      with the FK in the request body, under the old `@JsonIgnore` behavior)
- [x] 3.5 Confirmed fix via the same `curl` + DB check after the annotation
      change (`fk_id_presupuesto` correctly persisted)

## 4. Implementación

- [x] 4.1 Fix `Item.fkIdPresupuesto`: `@JsonIgnore` → `@JsonProperty(access =
      WRITE_ONLY)`; restore missing `jakarta.persistence.Version` import
      broken by a prior uncommitted edit
- [x] 4.2 Rebuild backend Docker image and redeploy; verify fix live
- [x] 4.3 Complete `historial/` Bruno folder: full CRUD lifecycle, chai assertions
- [x] 4.4 Complete `items/` Bruno folder: full CRUD lifecycle including the
      budget-FK happy path, chai assertions
- [x] 4.5 Complete `pagos/` Bruno folder: happy path payment against a real
      balance, over-limit 409 error path, chai assertions
- [x] 4.6 Complete `tramites/` Bruno folder: full CRUD lifecycle, chai assertions
- [x] 4.7 Finish/validate WIP in `00-auth/`, `auditoria/`, `folios/`,
      `inmueble/`, `plantilla-presupuesto/`, `suplencias/`
- [x] 4.8 File a follow-up GitHub Issue (linked to CU76) for the ~16
      controllers with zero Bruno coverage (see proposal.md Out of Scope) — #953
- [x] 4.9 Fix regression surfaced while authoring `historial/`: `DELETE
      /api/v1/historial/{id}` silently no-op'd for rows loaded fresh from the
      DB — `Historial`, `Item`, `Pago`, `Tramite` now implement
      `Persistable<Integer>`; `HistorialController.delete` unlinks the entity
      from `EstadoDeGestion.historialList` before deleting (Hibernate cascade
      on that stale collection otherwise cancels the delete)
- [x] 4.10 Migrate `InmuebleController` off the legacy `InmuebleJpaController`
      onto `InmuebleRepository`, needed to write the `inmueble/` folder

## 5. Actualizar tests existentes

- [x] 5.1 No existing Java unit/integration tests are affected by the
      annotation fix (see design.md Regression Strategy)
- [x] 5.2 Fixed `SimpleControllersTest$HistorialControllerTests.writeEndpoints`,
      broken by 4.9's `delete` behavior change (mocked repo needed `findById`
      + a linked `EstadoDeGestion.historialList`, not just `existsById`)
- [x] 5.3 Added `HistorialDeleteIntegrationTest` — reproduces the two-transaction
      delete-no-op bug (4.9) against H2 before the fix, passes after

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — 1745/1745 passing
- [x] 6.2 `mvn jacoco:check -pl backend-api` — gate passed (bound to `verify`)
- [x] 6.3 `mvn verify -pl backend-api` — BUILD SUCCESS
- [x] 6.4 `bru run . -r --env Developmen` (from `backend-api/api-test/`) —
      149/149 requests, 266/266 tests passing
- [x] 6.5 No `@Disabled`/skipped tests introduced

## 7. Ejecutar Playwright

- [x] 7.1–7.3 n/a — no UI surface (see design.md Playwright Strategy)
- [x] 7.4 Recorded above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `COVERAGE.md`, `TEST-PLAN.md` §7, `CU-API-MATRIX.csv`
      (§7 needed no edit — already generic, points at `COVERAGE.md`/CSV)
- [ ] 8.2 N/A — no endpoint contract/shape change, OpenAPI already documents
      `POST/PUT /api/v1/items`
- [x] 8.3 `CHANGELOG.md` entry for the four defects found/fixed
- [ ] 8.4 N/A — no documents to archive
- [x] 8.5 Confirmed no duplication introduced
- [x] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1–9.4 Pending — see `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1–10.5 Pending

## 11. Deploy

- [ ] 11.1–11.3 Pending

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1–12.4 Pending

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (this change)
- [x] Tests designed; fix proven failing-then-passing manually before the
      Bruno assertions are committed
- [ ] Full suite green: unit, integration, regression (Bruno)
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright n/a — no UI surface
- [ ] Permanent documentation updated
- [ ] Commits atomic and conventional, referencing #952
- [ ] PR created, CI green
- [ ] Merged, deployed, smoke test passed, Issue closed
- [ ] `traceability.md` complete from Issue through Release
