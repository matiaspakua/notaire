> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #583 is open and linked to CU76
- [x] 1.2 Acceptance criteria are the scenario in the delta spec
- [x] 1.3 Impact Analysis is in proposal.md
- [x] 1.4 No ADR

## 2. Crear branch

- [x] 2.1 Branch `cursor/583-ddl-auto-none-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 The guard test reads application.properties

## 4. Implementación

- [x] 4.1 ddl-auto is the literal none
- [x] 4.2 The environment placeholder is gone

## 5. Actualizar tests existentes

- [x] 5.1 CiDdlAutoNoneTest stays

## 6. Ejecutar regresión

- [x] 6.1 test_ddl_auto_pinned.py passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI change

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 No permanent product doc beyond the properties comment
- [x] 8.2 No API shape change

## 9. Commits atómicos

- [x] 9.1 Conventional commit
- [x] 9.2 The issue stays open, so the message does not use a closing keyword

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, push, open the PR. Refs #583

## 11. Deploy

- [ ] 11.1 Merge only when preflight exited 0 and required checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open for profiles and the default schema check

## Definition of Done

- [x] Issue linked to a Use Case
- [x] Specification written
- [x] Guard test passed
- [ ] Full suite green via preflight
- [ ] Commit does not close #583
- [ ] PR created
- [ ] Issue stays open after merge
