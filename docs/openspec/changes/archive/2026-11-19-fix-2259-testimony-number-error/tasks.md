> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2259 is open and linked to CU76
- [x] 1.2 Acceptance criteria are the two scenarios in the delta spec
- [x] 1.3 Impact Analysis is in proposal.md
- [x] 1.4 No ADR

## 2. Crear branch

- [x] 2.1 Branch `cursor/2259-testimony-number-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 The invalid-number cases expect a field error, not a toast
- [x] 3.2 Typing a valid number clears the error

## 4. Implementación

- [x] 4.1 Blank or non-integer save sets the FormField error
- [x] 4.2 A valid number and closing the dialog clear the error
- [x] 4.3 FORMS-AUDIT-PLAN.md records the finding

## 5. Actualizar tests existentes

- [x] 5.1 The toast expectation was the defect and was replaced
- [x] 5.2 The other testimony tests are unchanged

## 6. Ejecutar regresión

- [x] 6.1 `testimonies-page-actions.test.tsx` — 27 passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 No new spec: the edit dialog needs a testimony row and the shared list can be empty

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 FORMS-AUDIT-PLAN.md updated
- [x] 8.2 No API change

## 9. Commits atómicos

- [x] 9.1 Conventional commit
- [x] 9.2 The issue stays open, so the message does not use a closing keyword

## 10. Pull Request y validación CI

- [ ] 10.1 Archive, push, open the PR. Refs #2259

## 11. Deploy

- [ ] 11.1 Merge only when preflight exited 0 and required checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open for the remaining modules

## Definition of Done

- [x] Issue linked to a Use Case
- [x] Specification written
- [x] Tests written for the field error
- [ ] Full suite green via preflight
- [x] Permanent documentation updated
- [x] Commit does not close #2259
- [ ] PR created
- [ ] Issue stays open after merge
