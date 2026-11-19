> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2259 is open and linked to CU76
- [x] 1.2 Acceptance criteria are the two scenarios in the delta spec
- [x] 1.3 Impact Analysis is in proposal.md
- [x] 1.4 No ADR

## 2. Crear branch

- [x] 2.1 Branch `cursor/2259-protocol-folios-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests

- [x] 3.1 The notebook test expected aria-invalid and failed first
- [x] 3.2 Toggling the folio is covered

## 4. Implementación

- [x] 4.1 Create without a notary sets the FormField error
- [x] 4.2 CheckboxField reads the FormField state
- [x] 4.3 Changing the selection clears the error

## 5. Actualizar tests existentes

- [x] 5.1 No previous notebook behaviour test expected the toast

## 6. Ejecutar regresión

- [x] 6.1 `notebooks-page-actions.test.tsx` — 2 passed after the fix
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 No new spec: the dialog needs folio rows and the shared list can be empty

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
- [x] Tests failed first, then passed
- [ ] Full suite green via preflight
- [x] Permanent documentation updated
- [x] Commit does not close #2259
- [ ] PR created
- [ ] Issue stays open after merge
