> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2007 and CU78
- [x] 1.2 Scope is the model, not a new audit annotation
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/2007-linddun-records-47dc` from `origin/main` at `7f1feeb0`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Expected LD ids extended first
- [x] 3.2 Observed LD-7, LD-8, and LD-9 missing from the register

## 4. Implementación

- [x] 4.1 Add LD-7, LD-8, and LD-9 to the model and the register
- [x] 4.2 Point LD-4 and LD-5 at the program section

## 5. Actualizar tests existentes

- [x] 5.1 The id equality test now includes the three rows

## 6. Ejecutar regresión

- [x] 6.1 Threat-register and assessment-currency tests
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Threat model, register, program, and assessment

## 9. Commits atómicos

- [x] 9.1 Conventional commit referencing #2007

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. The issue closes when the rows are on main.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0, required checks are green, and the branch is clean

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 No runtime change. LD-9 stays open inside the model.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open and the rows merged
