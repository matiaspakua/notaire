# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #178 exists with CU25
- [x] 1.2 Board/label write retried; 403 recorded
- [x] 1.3 Specification written in this change

## 2. Crear branch

- [x] 2.1 `cursor/178-monthly-sworn-statement-5fcb` from updated `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Write failing builder, locale, integration, and controller tests
- [x] 3.2 Write failing Vitest hook/page and Playwright CU25 download tests
- [x] 3.3 Observe the failure before production code

## 4. Implementación

- [x] 4.1 `MonthlySwornStatementReportBuilder`
- [x] 4.2 Wire `ReportService` / `ReportController` optional filters
- [x] 4.3 UI year/month plus optional notary/protocol; unify filename

## 5. Actualizar tests existentes

- [x] 5.1 Keep year+month AdditionalControllers / Bruno / integration 200
- [x] 5.2 Update skip inventory after unskipping CU25

## 6. Ejecutar regresión

- [x] 6.1 Targeted backend and frontend tests green
- [ ] 6.2 Pipeline / preflight as far as the environment allows

## 7. Ejecutar Playwright

- [x] 7.1 CU25 seed + download + `%PDF-` + extracted text (test written; CI runs it)

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CU25, REST reference, SAD, E2E mapping, CHANGELOG Unreleased #178

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; completing commit / PR body `Closes #178`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 Merge only after `check-heavy-ci.sh` exits 0

## 11. Deploy

- [ ] 11.1 Owner deploys after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Authenticated smoke download; issue/board close (needs write access)

## Definition of Done

- [ ] CU25 sworn statement lists month deeds with titular/substitute when recorded
- [ ] Tax/rate/annex are N/A (no invented legal copy)
- [ ] Empty period is 200 + empty-state PDF
- [ ] Year+month-only clients still work
- [ ] Playwright CU25 is unskipped
- [ ] Docs and CHANGELOG updated
- [ ] PR `Closes #178`
