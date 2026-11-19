> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2325 and CU76
- [x] 1.2 Scope is the three measured gaps
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from updated main

## 3. Gate 2 — Escribir tests

- [x] 3.1 Tests written first
- [x] 3.2 Embed count was 1; gate line said adoption 0%

## 4. Implementación

- [x] 4.1 Skip embed_query for an exact identifier
- [x] 4.2 Add calls predecessors
- [x] 4.3 adoption n/a when sessions is 0

## 5. Actualizar tests existentes

- [x] 5.1 Existing search and 50% adoption tests still pass
- [x] 5.2 Version 0.4.4 and changelog

## 6. Ejecutar regresión

- [x] 6.1 The four targeted tests passed
- [ ] 6.2 verify.sh and preflight.sh --full

## 7. Ejecutar Playwright

- [x] 7.1 No product UI

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Changelog and factory KPI files

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with Refs #2325

## 11. Deploy

- [ ] 11.1 Merge when preflight and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Close #2325 only when this slice is merged

## Definition of Done

- [x] Failing tests observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
