> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #2259 is open and linked to CU76
- [x] 1.2 Use Case documentation exists
- [x] 1.3 Acceptance criteria are the three scenarios in the delta spec
- [x] 1.4 Impact Analysis is in proposal.md
- [x] 1.5 No ADR: the form pattern already exists
- [x] 1.6 WIP label SKIPPED if the label API returns 403

## 2. Crear branch

- [x] 2.1 Branch `cursor/2259-wait-reason-47dc` from `origin/main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 The hold-reason test now expects a field error, not a toast
- [x] 3.2 Typing and reopening are covered in the same test
- [x] 3.3 The previous assertion was `toast.error` for `errorMotivoRequerido`

## 4. Implementación

- [x] 4.1 Blank confirm sets the FormField error and sends nothing
- [x] 4.2 A non-blank reason, and closing the dialog, clear the error
- [x] 4.3 FORMS-AUDIT-PLAN.md records the finding

## 5. Actualizar tests existentes

- [x] 5.1 The hold-reason test was updated because the toast expectation was the defect
- [x] 5.2 The other managements action tests are unchanged

## 6. Ejecutar regresión

- [x] 6.1 `managements-page-actions.test.tsx` — 21 passed
- [ ] 6.2 `bash workspace/sdlc/preflight.sh --full`

## 7. Ejecutar Playwright

- [x] 7.1 No new spec: the hold dialog needs a folder row and the shared E2E list can be empty
- [x] 7.2 `TS-0159` still covers the create dialog and runs inside preflight

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 FORMS-AUDIT-PLAN.md updated
- [x] 8.2 No API change
- [x] 8.3 CHANGELOG is release-please
- [x] 8.4 No superseded document

## 9. Commits atómicos

- [x] 9.1 Conventional commit
- [x] 9.2 The issue stays open, so the message does not use a closing keyword
- [x] 9.3 No secrets

## 10. Pull Request y validación CI

- [ ] 10.1 Archive before the PR
- [ ] 10.2 Push
- [ ] 10.3 Open the PR. Refs #2259. The issue stays open

## 11. Deploy

- [ ] 11.1 Merge only when preflight exited 0 and required checks are green

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 The issue stays open for the remaining modules

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written
- [x] Tests written for the field error
- [ ] Full suite green via preflight
- [x] Coverage floors are not lowered
- [x] Create-dialog Playwright spec remains
- [x] Permanent documentation updated
- [x] Commit does not close #2259
- [ ] PR created
- [ ] Issue stays open after merge
