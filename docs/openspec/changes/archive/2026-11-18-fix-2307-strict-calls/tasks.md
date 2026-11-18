> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2307 and CU76
- [x] 1.2 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/2307-strict-calls-47dc`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed: `Reader.show` linked `Folio.getDto`

## 4. Implementación

- [x] 4.1 Strict same-directory calls must be unique
- [x] 4.2 Version 0.4.2

## 5. Actualizar tests existentes

- [x] 5.1 `test_structural_edges` still passed

## 6. Ejecutar regresión

- [x] 6.1 `verify.sh` exit 0 (70 passed, coverage 91.13%, wheel 0.4.2)
- [ ] 6.2 `preflight.sh --full` before push

## 7. Ejecutar Playwright

- [x] 7.1 No product UI

## 8. Gate 3 — Documentación permanente

- [x] 8.1 CHANGELOG and factory logs

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 PR with `Refs #2307`

## 11. Deploy

- [ ] 11.1 Owner merges

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 #2307 stays open until the owner closes the remaining friction

## Definition of Done

- [x] Ambiguous call is not guessed
- [x] `verify.sh` exit 0
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
