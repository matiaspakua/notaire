> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2010 and CU78
- [x] 1.2 This slice is the per-JVM quota only
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from updated main

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed to compile: quota class missing

## 4. Implementación

- [x] 4.1 Fixed window
- [x] 4.2 Filter returns 429
- [x] 4.3 Chain runs after JWT authentication

## 5. Actualizar tests existentes

- [x] 5.1 Security config test still passes
- [x] 5.2 Threat register test still passes

## 6. Ejecutar regresión

- [x] 6.1 Targeted tests passed
- [ ] 6.2 preflight.sh --full before push

## 7. Ejecutar Playwright

- [x] 7.1 No UI change. Default ceiling stays above the local suite

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Threat model and STRIDE register

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with Refs #2010

## 11. Deploy

- [ ] 11.1 Merge when preflight and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open until the counter is shared

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open
