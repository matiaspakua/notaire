> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 Issue #596 exists
- [x] 1.2 CU70 already exists
- [x] 1.3 This slice is GET /copies only
- [x] 1.4 Impact analysis is in `proposal.md`
- [x] 1.5 No new ADR
- [x] 1.6 WIP label via `add-issue-wip-label.sh 596` (SKIPPED on 403 is honest)

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 Branch `cursor/596-copies-page-47dc`
- [x] 2.3 Branch recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 The list returns one page
- [x] 3.2 `SimpleControllersTest` updated first
- [x] 3.3 Bruno reads `content`
- [x] 3.4 The test failed to compile: findPage missing
- [x] 3.5 The cases map to that test

## 4. Implementación

- [x] 4.1 `findPage` on the query port and adapter
- [x] 4.2 Controller returns `Page<CopyResponse>`
- [x] 4.3 The screen uses `useCopiesPage` and the pager

## 5. Actualizar tests existentes

- [x] 5.1 Integration test expects `content`
- [x] 5.2 The form test still opens the dialog
- [x] 5.3 No test removed

## 6. Ejecutar regresión

- [x] 6.1 Copy controller test passed
- [x] 6.2 JaCoCo floor unchanged
- [x] 6.3 Full gates run inside `preflight.sh --full`
- [x] 6.4 Bruno list check reads `content`
- [x] 6.5 No skipped tests added

## 7. Ejecutar Playwright

- [x] 7.1 Existing copy screens cover the list
- [x] 7.2 Newest copy stays on page 1
- [x] 7.3 n/a
- [x] 7.4 `preflight.sh --full` runs the suite

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 OpenAPI documents page, size and sort
- [x] 8.2 `api.generated.ts` regenerated
- [x] 8.3 CHANGELOG n/a
- [x] 8.4 Nothing else to archive
- [x] 8.5 No duplicated permanent doc
- [x] 8.6 `preflight.sh --full` is the gate

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 `Refs #596`
- [ ] 9.3 No secrets
- [ ] 9.4 Record the SHA

## 10. Pull Request y validación CI

- [ ] 10.1 Archive the change
- [ ] 10.2 Push
- [ ] 10.3 Open the PR
- [ ] 10.4 Local preflight is the proof (#2001)
- [ ] 10.5 Owner reviews
- [ ] 10.6 Record the PR number

## 11. Deploy

- [ ] 11.1 Owner merges
- [ ] 11.2 No image publish on this pull request
- [ ] 11.3 Record the merge later

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Copies list is the smoke
- [ ] 12.2 Revert is safe
- [ ] 12.3 Do not close #596
- [ ] 12.4 Record later

## Definition of Done

- [ ] GET /copies is paged and the screen reads that page
- [ ] Failing test observed
- [ ] `preflight.sh --full` exit 0
- [ ] PR open
