# Tasks

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #1593 exists with CU76 and acceptance criteria
- [x] 1.2 Board/label write retried; 403 recorded; Owner Continue honored
- [x] 1.3 Specification written in this change

## 2. Crear branch

- [x] 2.1 `feat/1593_pdf-report-design-system` from updated `main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Write failing unit tests for theme, assets, sanitizer, formats, renderer, headers, metrics, structural raster
- [x] 3.2 Observe the failure before production code (compile-red, then green)

## 4. Implementación

- [x] 4.1 Theme, asset catalog, sanitizer, locale formats, chrome, renderer, clock bean
- [x] 4.2 PdfResponseFactory + structured 500 + notebook cover headers
- [x] 4.3 Migrate seven hand-built PDFs onto ReportDocument
- [x] 4.4 Generation metrics without PII

## 5. Actualizar tests existentes

- [x] 5.1 Update renderer `Page` assertions to `Página`
- [x] 5.2 Update receipt tests to PDFTextStripper
- [x] 5.3 Update controller header/500 assertions without weakening them

## 6. Ejecutar regresión

- [x] 6.1 Targeted backend tests green
- [ ] 6.2 `bash workspace/sdlc/run_pipeline.sh` as far as the environment allows

## 7. Ejecutar Playwright

- [x] 7.1 No UI change — skip new Playwright; record in traceability

## 8. Gate 3 — Documentación permanente

- [x] 8.1 SAD design-system doc + sad.md pointer
- [x] 8.2 Security input-validation note
- [x] 8.3 CU76 link and CHANGELOG Unreleased

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; completing commit / PR body `Closes #1593`

## 10. Pull Request y validación CI

- [ ] 10.1 Push and open PR
- [ ] 10.2 Preflight; merge only after `check-heavy-ci.sh` exits 0

## 11. Deploy

- [ ] 11.1 Owner deploys after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Authenticated smoke download; issue/board close (needs write access)

## Definition of Done

- [ ] Shared theme, embedded font, wordmark, chrome, headers, metrics, and tests land
- [ ] Existing business fields retained
- [ ] Docs and CHANGELOG updated
- [ ] PR `Closes #1593`
