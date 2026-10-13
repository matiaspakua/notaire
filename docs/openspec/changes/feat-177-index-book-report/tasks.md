# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #177 exists with CU24
- [x] 1.2 Board/label write retried; 403 recorded; Owner Continue honored
- [x] 1.3 Specification written in this change

## 2. Crear branch

- [x] 2.1 `cursor/177-index-book-report-5fcb` from updated `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Write failing builder, locale, integration, and controller tests
- [x] 3.2 Write failing Vitest hook/page and Playwright CU24 download tests
- [x] 3.3 Observe the failure before production code

## 4. Implementación

- [x] 4.1 `IndexBookReportBuilder` + optional Folio/Procedure queries
- [x] 4.2 Wire `ReportService` / `ReportController` optional filters
- [x] 4.3 UI year/notary/protocol fields and hook query string

## 5. Actualizar tests existentes

- [x] 5.1 Keep year-only AdditionalControllers / Bruno / integration 200
- [x] 5.2 Update skip inventory after unskipping CU24

## 6. Ejecutar regresión

- [x] 6.1 Targeted backend and frontend tests green
- [ ] 6.2 Pipeline / preflight as far as the environment allows

## 7. Ejecutar Playwright

- [ ] 7.1 CU24 seed + download + `%PDF-` + extracted text

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CU24, REST reference, SAD, E2E mapping, CHANGELOG Unreleased #177

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; completing commit / PR body `Closes #177`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 Merge only after `check-heavy-ci.sh` exits 0

## 11. Deploy

- [ ] 11.1 Owner deploys after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Authenticated smoke download; issue/board close (needs write access)

## Definition of Done

- [ ] CU24 index book queries recorded deeds and prints sorted person rows
- [ ] Empty period is 200 + empty-state PDF
- [ ] Year-only clients still work
- [ ] Playwright CU24 is unskipped with a data builder
- [ ] Docs and CHANGELOG updated
- [ ] PR `Closes #177`
