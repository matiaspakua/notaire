# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #203 exists with CU50
- [x] 1.2 Board/label write retried; 403 recorded if it fails
- [x] 1.3 Specification written in this change

## 2. Crear branch

- [x] 2.1 `cursor/203-tax-sworn-statement-5fcb` from updated `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Write failing builder, integration, and controller tests
- [x] 3.2 Write failing Vitest hook/page and Playwright CU50 download tests
- [x] 3.3 Observe the failure before production code

## 4. Implementación

- [x] 4.1 `TaxSwornStatementReportBuilder`
- [x] 4.2 Wire `ReportService` / `ReportController` optional filters
- [x] 4.3 UI year/month plus optional notary/protocol; unify filename

## 5. Actualizar tests existentes

- [x] 5.1 Keep year+month AdditionalControllers / Bruno / integration 200
- [x] 5.2 Update E2E mapping after adding CU50-GW01

## 6. Ejecutar regresión

- [x] 6.1 Targeted backend and frontend tests green
- [x] 6.2 Pipeline / preflight as far as the environment allows

## 7. Ejecutar Playwright

- [x] 7.1 CU50 seed + download + `%PDF-` + extracted text

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CU50, REST reference, SAD, E2E mapping, INVENTORY, CHANGELOG Unreleased #203

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; completing commit / PR body `Closes #203`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 Merge only after `check-heavy-ci.sh` exits 0

## 11. Deploy

- [ ] 11.1 Owner deploys after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Authenticated smoke download; issue/board close (needs write access)

## Definition of Done

- [ ] CU50 tax sworn statement lists month deeds with titular/substitute when recorded
- [ ] Tax/operation/payment-form are N/A (no invented legal copy)
- [ ] Empty period is 200 + empty-state PDF
- [ ] Year+month-only clients still work
- [ ] Playwright CU50-GW01 is present
- [ ] Docs and CHANGELOG updated
- [ ] PR `Closes #203`
