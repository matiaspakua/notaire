> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #23, CU15/RF-21
- [x] 1.2 Use Case documentation exists and is accurate — `CU15 – Procesar pago.md`
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a, not architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 23 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/23_recibo-de-pago-pdf`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: pago simple, pago parcial/en cuotas, pago inexistente (404)
- [x] 3.2 Write `ReporteServiceReciboPagoTest` (unit) for all three scenarios
- [x] 3.3 Write `ReciboPagoReportIntegrationTest#shouldReturnPdfForRecibo` / `...404ForNonExistingPago` (integration, H2)
- [x] 3.4 Run them and observe them fail — `mvn test -pl backend-api -Dtest=ReporteServiceReciboPagoTest`
- [x] 3.5 Confirm every `#### Scenario:` in `specs/pago-recibo/spec.md` maps to a test

## 4. Implementación

- [x] 4.1 `ReporteService`: add `generarReporteReciboPago(Integer idPago)` — fetch `Pago` (+ `Presupuesto`, `Persona`, `Item`s), throw `ResourceNotFoundException` if missing
- [x] 4.2 `ReporteService`: build the recibo content stream (cliente, fecha, concepto(s), total) via the existing `buildPdf(String)` primitive
- [x] 4.3 `ReporteController`: add `GET /api/v1/reportes/recibo-pago/{idPago}` (own try/free path via `ResourceNotFoundException` + `GlobalExceptionHandler`, matching `minuta-inscripcion`)
- [x] 4.4 `frontend/src/hooks/useReportes.ts`: add `useReciboPago()` hook (same `downloadPdf` pattern)
- [x] 4.5 `frontend/src/app/dashboard/pagos/page.tsx`: add "Emitir recibo" action per row, wired to the new hook

## 5. Actualizar tests existentes

- [x] 5.1 None expected — additive endpoint, no existing test touches `ReporteService`/`ReporteController` recibo behavior
- [x] 5.2 n/a
- [x] 5.3 n/a

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — 1752 tests, 0 failures
- [x] 6.2 `mvn jacoco:check -pl backend-api` (via `mvn verify`) — coverage checks met
- [x] 6.3 `mvn verify -pl backend-api` — BUILD SUCCESS
- [x] 6.4 Added Bruno request `backend-api/api-test/pagos/10-get-recibo-pdf.yml` (renumbered delete/verify-delete to seq 12/13)
- [x] 6.5 No skipped tests

## 7. Ejecutar Playwright

- [x] 7.1 Extend `frontend/tests/e2e/TS-0014-pagos-workflow.spec.ts`: click "Emitir recibo" after creating a pago, assert the PDF response
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the pagos screen action at 320px, 768px, 1024px
- [x] 7.4 n/a — has UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/100-business/102-use-cases/CU15 – Procesar pago.md`, `docs/300-development/303-testing/CU-API-MATRIX.csv`, `backend-api/api-test/COVERAGE.md`
- [x] 8.2 Add `@Operation`/`@ApiResponse` to the new controller method; verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` `[Unreleased]`
- [ ] 8.4 n/a — no document superseded
- [x] 8.5 Confirm no duplication
- [x] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [x] 9.1 Small, self-contained commits, Conventional Commits format
- [x] 9.2 Commits reference `#23` (`Refs #23`); the PR carries `Fixes #23` per `general.md` §6.1 (only the closing artifact uses `Closes`)
- [x] 9.3 No secrets, no commented-out code
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/23_recibo-de-pago-pdf`
- [ ] 10.2 Open PR `[#23] feat(reportes): emitir recibo de pago en PDF`
- [ ] 10.3 Wait for CI green
- [ ] 10.4 Gate 4 — CI green, no conflicts, docs complete
- [ ] 10.5 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm CD pipeline published the image
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test: `GET /api/v1/reportes/recibo-pago/{idPago}` on deployed env returns `200`/`application/pdf`
- [ ] 12.2 Verify rollback path (design.md)
- [ ] 12.3 Close Issue #23, referencing the PR
- [ ] 12.4 `openspec archive recibo-de-pago-pdf`

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
