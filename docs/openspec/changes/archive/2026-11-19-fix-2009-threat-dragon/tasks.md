> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #2009 and CU78
- [x] 1.2 The upstream v2 schema is public and checkable
- [x] 1.3 WIP label SKIPPED on 403

## 2. Crear branch

- [x] 2.1 Branch from `origin/main` at `19eaefc7`

## 3. Gate 2 — Escribir tests

- [x] 3.1 Test written first
- [x] 3.2 It failed because the schema file was missing

## 4. Implementación

- [x] 4.1 Vendor the schema and add the Notaire model
- [x] 4.2 Drop Threat Dragon from the not-adopted list

## 5. Actualizar tests existentes

- [x] 5.1 Register test no longer requires Threat Dragon in the not-adopted set

## 6. Ejecutar regresión

- [x] 6.1 Dragon, register, and assessment-currency tests
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface

## 8. Gate 3 — Documentación permanente

- [x] 8.1 Tools, register, assessment, and program

## 9. Commits atómicos

- [x] 9.1 Conventional commit

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, then open the PR. The issue closes when the check is on main.

## 11. Deploy

- [ ] 11.1 Merge when preflight exit 0 and checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 No runtime change. The issue is finished when the model check is on main.

## Definition of Done

- [x] Failing test observed
- [ ] preflight.sh --full exit 0
- [ ] PR open and the model check merged
