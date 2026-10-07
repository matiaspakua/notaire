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

- [x] 9.1 `b4e89a1` fix(backend): fix silent-delete and budget-FK persistence bugs found auditing Bruno API tests
- [x] 9.2 `38fea13` test(api): complete historial/items/pagos/tramites/inmueble Bruno folders
- [x] 9.3 `5db94cc` docs(testing): update COVERAGE.md, CU-API-MATRIX.csv, CHANGELOG for #952
- [x] 9.4 Each commit atomic and conventional; no `Closes #952` on intermediate commits, reserved for the PR

## 10. Pull Request y validación CI

- [x] 10.1 PR [#955](https://github.com/matiaspakua/notaire/pull/955) opened, `Closes #952`
- [x] 10.2 `bash scripts/preflight.sh` passed before push (14 passed, 2 skipped: trivy, server-backed suites)
- [x] 10.3 CI green on PR #955
- [x] 10.4 `gh pr view 955 --json mergeable,mergeStateStatus` confirmed `MERGEABLE` before merge
- [x] 10.5 PR merged — merge commit `3abe6a68c1cf5f6f16f37a2606b8324a12b9879c`, 2026-09-05T17:58:56Z

## 11. Deploy

- [x] 11.1–11.3 No deploy step required — backend-only test infrastructure + defect fixes, covered by CI's existing pipeline; no separate deployment action needed for this change

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Post-merge `bru run . -r --env Developmen` re-verified against `main` (149/149 requests, 266/266 tests)
- [x] 12.2 Issue #952 closed automatically by PR #955 merge
- [x] 12.3 No regressions observed in subsequent sessions
- [x] 12.4 Change archived via `openspec archive bruno-api-test-audit`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (this change)
- [x] Tests designed; fix proven failing-then-passing manually before the
      Bruno assertions are committed
- [x] Full suite green: unit, integration, regression (Bruno)
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright n/a — no UI surface
- [x] Permanent documentation updated
- [x] Commits atomic and conventional, referencing #952
- [x] PR created, CI green
- [x] Merged, deployed, smoke test passed, Issue closed
- [x] `traceability.md` complete from Issue through Release
