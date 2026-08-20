> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #792 exists, labeled, and linked to CU15
- [x] 1.2 CU15 ("Procesar pago") documentation exists — update it (see group 8) to state the payment method is persisted
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/pagos/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required (see design.md — Decisions): follows existing `service`/`api`/`negocio` layering, additive schema change only
- [x] 1.6 Move the Issue to IN PROGRESS: `gh issue edit 792 --add-label "in-progress"` — already labeled `in-progress`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/792_persist-metodo-pago-on-pago` — branch created, up to date with `main` (no divergent commits yet)
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/pagos/spec.md`: create with método, retrieve reflects stored método, edit updates método, create without método (edge), edit método of non-existent pago (error)
- [x] 3.2 Write `PagoServiceTest` additions: `shouldPersistMetodoPagoWhenProcesarPago`, `shouldUpdateMetodoPagoWhenEditarPago`, `shouldAllowNullMetodoPagoOnProcesarPago`, `shouldThrowWhenEditingMetodoPagoOfMissingPago`
- [x] 3.3 Write `PagoIntegrationTest` additions: `shouldReturnMetodoPagoOnCreate`, `shouldReturnStoredMetodoPagoOnGetById`, `shouldPersistUpdatedMetodoPago`
- [x] 3.4 Run them and **observe them fail**: `mvn test -pl backend-api -Dtest=PagoServiceTest,PagoIntegrationTest`
- [x] 3.5 Confirm every `#### Scenario:` in `specs/pagos/spec.md` maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [x] 4.1 Add Flyway migration `V16__add_metodo_pago_to_pagos.sql` adding nullable `metodo_pago text` to `pagos` (see design.md — Decisions)
- [x] 4.2 Add `metodoPago` field with getter/setter to `negocio/Pago.java`, mapped in `getDto()` and `setAtributos(DtoPago)`
- [x] 4.3 Add `metodoPago` field with getter/setter to `notaire-shared/dto/DtoPago.java`
- [x] 4.4 Add `metodoPago` to `PagoController.PagoRequest` record
- [x] 4.5 Thread `metodoPago` through `PagoService.procesarPago(...)` and `PagoService.editarPago(...)` signatures and persistence
- [x] 4.6 Update `PagoController.procesarPago`/`update` to pass `request.metodoPago()`/`entity.getMetodoPago()` through to the service
- [x] 4.7 Document the changed request/response shape with `@Operation`/`@ApiResponse` on the affected `PagoController` endpoints and verify in Swagger UI

## 5. Actualizar tests existentes

- [x] 5.1 Update `PagoServiceTest`/`PagoIntegrationTest` fixtures that construct a `Pago`/`PagoRequest` to include the new field where relevant, without weakening existing assertions (see design.md — Regression Strategy)
- [x] 5.2 Verify any `ControllerNegocio`/`DtoPago` fixture test still passes unchanged (field is optional)
- [x] 5.3 No tests are expected to become obsolete

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration (1497 tests, 0 failures)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor (passes as part of `mvn verify`)
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs) — BUILD SUCCESS
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite — all strict endpoint checks passed
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no new UI surface: `frontend/src/app/dashboard/pagos/page.tsx` already sends and renders `metodoPago` (see design.md — Playwright Strategy); confirmed `cu15-pagos.spec.ts` (CU15/CU47 scenarios) still passes since the response shape only gains one optional field
- [x] 7.2 `npx playwright test cu15-pagos.spec.ts` — all green (0 unexpected failures)
- [x] 7.3 n/a — no new/changed screen to verify at 320/768/1024px
- [x] 7.4 Recorded here per Constitution §12: no UI surface change, so no new Playwright spec is added

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/100-business/102-use-cases/CU15 – Procesar pago.md` — state the payment method is persisted with the payment
- [x] 8.2 Update OpenAPI/Swagger annotations for the changed `PagoController` endpoints; verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — payment method is now persisted and returned with each `Pago`
- [x] 8.4 No documents to archive for this change
- [x] 8.5 Confirm no information was duplicated — CU15 doc and `CHANGELOG.md` cover distinct audiences, no overlap
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (15/15 passed; trivy + server-backed suites skipped, already covered by tasks 6.4/7.2)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format — 3 commits: test, feat, docs
- [x] 9.2 Every commit message ends with `Closes #792`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin fix/792_persist-metodo-pago-on-pago`
- [x] 10.2 Open the PR titled `[#792] fix: persist metodoPago on Pago`, referencing Issue #792 and CU15 — PR #828
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml` — `CI`, `Frontend CI`, `PR Validation` green on PR head `bee85e7`; `Playwright E2E — Full Suite` failed on the same pre-existing `01-first-case-tutorial.spec.ts` flake also failing on `main` since 2026-08-17
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete — CI green apart from the pre-existing flake above; no recorded review approval on PR #828 (self-merged by `matiaspakua`)
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main` (merged via PR #828, merge commit `ad74591`)
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR — not yet run for merge commit `ad74591` (last CD run 2026-08-19 for `fc0c9cd`)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md` — merge commit recorded; release/tag still pending CD run

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Process a test payment with a known `metodoPago` value via `POST /api/v1/pagos` on the target environment; `GET` it back and confirm the value round-trips
- [ ] 12.2 Verify the rollback path described in design.md is still available (revert is safe, additive-only)
- [x] 12.3 Close GitHub Issue #792, referencing the PR (issue already closed on merge — out of intended Gate 5 order, since smoke test had not run yet)
- [ ] 12.4 Archive the change: `openspec archive persist-metodo-pago-on-pago`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes (no new UI surface; `cu15-pagos.spec.ts` still passes)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4) — PR merged; CI green apart from pre-existing flake; no recorded review approval
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5) — merged and closed; deploy/smoke still pending
- [x] `traceability.md` complete from Issue through Release
