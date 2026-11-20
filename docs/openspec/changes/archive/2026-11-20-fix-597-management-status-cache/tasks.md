> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #597 is open and linked to CU74
- [x] 1.2 Acceptance criteria are the two scenarios in the delta spec
- [x] 1.3 Impact Analysis is in proposal.md
- [x] 1.4 No ADR

## 2. Crear branch

- [x] 2.1 Branch `cursor/597-management-status-cache-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 The cache test counts port reads
- [x] 3.2 A write must make the next list see the new row

## 4. Implementación

- [x] 4.1 Management-status list uses the existing Caffeine cache, 60 second TTL
- [x] 4.2 the outbound catalog decorator's findAll is @Cacheable; save and delete evict it
- [x] 4.3 create, update, and delete evict the list

## 5. Actualizar tests existentes

- [x] 5.1 Management-status use-case tests still pass

## 6. Ejecutar regresión

- [x] 6.1 ManagementStatusCatalogCacheTest passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 No permanent product doc beyond this change
- [x] 8.2 No API shape change

## 9. Commits atómicos

- [x] 9.1 Conventional commit
- [x] 9.2 The issue stays open, so the message does not use a closing keyword

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, push, open the PR. Refs #597

## 11. Deploy

- [ ] 11.1 Merge only when preflight exited 0 and required checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open for the other catalogs

## Definition of Done

- [x] Issue linked to a Use Case
- [x] Specification written
- [x] Cache test passed
- [ ] Full suite green via preflight
- [ ] Commit does not close #597
- [ ] PR created
- [ ] Issue stays open after merge
