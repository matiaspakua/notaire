> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #580 and CU76
- [x] 1.2 This slice only deletes unused History.getDto
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/580-history-mapper-47dc` from updated main

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed: getDto was still declared

## 4. Implementación

- [x] 4.1 Delete getDto and DTO imports
- [x] 4.2 Shrink the ratchet

## 5. Actualizar tests existentes

- [x] 5.1 HistoryMapperTest still passes
- [x] 5.2 No test removed

## 6. Ejecutar regresión

- [x] 6.1 Targeted tests passed
- [ ] 6.2 preflight.sh --full before push

## 7. Ejecutar Playwright

- [x] 7.1 No UI change

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Factory comparison row
- [x] 8.2 No product doc and no Pages edit

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with Refs #580

## 11. Deploy

- [ ] 11.1 Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Do not close #580

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
