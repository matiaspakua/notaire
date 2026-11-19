> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2329 and CU76
- [x] 1.2 This slice is Procedure.getDto only
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from updated main

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed to compile: mapper missing

## 4. Implementación

- [x] 4.1 Add toDto
- [x] 4.2 Deed and testimony mappers call it
- [x] 4.3 Delete Procedure.getDto

## 5. Actualizar tests existentes

- [x] 5.1 ProcedureEntityTest uses the mapper
- [x] 5.2 Ratchet line removed

## 6. Ejecutar regresión

- [x] 6.1 Targeted tests passed
- [ ] 6.2 preflight.sh --full before push

## 7. Ejecutar Playwright

- [x] 7.1 No UI change

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Factory KPI note for the second refresh

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with Refs #2329

## 11. Deploy

- [ ] 11.1 Merge when preflight and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
