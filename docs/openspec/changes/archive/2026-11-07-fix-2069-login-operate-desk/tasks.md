> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #2069 exists, labeled, and linked to CU84
- [ ] 1.2 Use Case CU84 exists and this change does not alter its credential flow
- [ ] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR not required (presentation only)
- [ ] 1.6 Issue #2069 labeled `status:in-progress`

## 2. Crear branch

- [ ] 2.1 Worktree from updated `origin/main` (do not touch the ProcedureFolder hold worktree)
- [ ] 2.2 Branch `fix/2069_login-operate-desk`
- [ ] 2.3 Branch name recorded in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Cases: desk card + headings; seal source has no rotate or spring; reduced-motion transform is none
- [ ] 3.2 Unit test in `frontend/src/tests/unit/login-page.test.tsx`
- [ ] 3.3 No new integration test (no API change)
- [ ] 3.4 Run the new unit test and observe it fail
- [ ] 3.5 Both delta scenarios map to that unit test; the reduced-motion clause also maps to TS-0001

## 4. Implementación

- [ ] 4.1 Login page: one card, h1/h2, three token motions
- [ ] 4.2 Keep FormContainer, FormField, FormActions, and existing test ids
- [ ] 4.3 No new colors; use existing card radius and `apple-shadow`

## 5. Actualizar tests existentes

- [ ] 5.1 `login-page.test.tsx` lockout, session-expired, and #1055 cases stay
- [ ] 5.2 No assertion weakened
- [ ] 5.3 No test removed

## 6. Ejecutar regresión

- [ ] 6.1 Frontend unit test for the login page
- [ ] 6.2 Coverage floors unchanged
- [ ] 6.3 `bash workspace/sdlc/preflight.sh --full` is the PR gate
- [ ] 6.4 Bruno runs inside that preflight; no API change
- [ ] 6.5 No skipped tests added

## 7. Ejecutar Playwright

- [ ] 7.1 Extend `TS-0001-login-authentication.spec.ts`
- [ ] 7.2 Run that spec when the stack is up; full Playwright is inside `preflight.sh --full`
- [ ] 7.3 Confirm the card at 320px (no horizontal overflow), 768px, and 1024px
- [ ] 7.4 UI surface is `/login`

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `DESIGN.md`, `PRODUCT.md`, design-system note, fleet lesson
- [ ] 8.2 OpenAPI unchanged
- [ ] 8.3 `CHANGELOG.md` `[Unreleased]`
- [ ] 8.4 Nothing to archive from permanent docs
- [ ] 8.5 Design-system note points at `DESIGN.md` instead of copying the token catalog
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full`

## 9. Commits atómicos

- [ ] 9.1 One conventional commit for the tip
- [ ] 9.2 Commit message uses `Refs #2069` (not `Closes`), so sibling backlog issues are not auto-closed
- [ ] 9.3 No secrets and no unrelated dirty files from the primary checkout
- [ ] 9.4 Record the commit SHA in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `cd docs && openspec archive fix-2069-login-operate-desk --yes` in the PR
- [ ] 10.2 Push `fix/2069_login-operate-desk`
- [ ] 10.3 Open the PR with `Refs #2069`, Deviations, AI assistance, Review budget, and Risk
- [ ] 10.4 GitHub does not re-run the full suite on the PR (#2001); local `preflight.sh --full` is the gate
- [ ] 10.5 Gate 4 waits on review
- [ ] 10.6 Record the PR number in the archived traceability file

## 11. Deploy

- [ ] 11.1 Merge via the pull request only
- [ ] 11.2 CD publishes on merge to `main`
- [ ] 11.3 Record the merge commit after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke `/login` and one admin sign-in after deploy
- [ ] 12.2 Revert is safe (no schema)
- [ ] 12.3 Close #2069 after merge, referencing the PR
- [ ] 12.4 Record smoke evidence after deploy

## Definition of Done

- [ ] Issue linked to CU84, with Acceptance Criteria
- [ ] Specification written (Gate 1)
- [ ] Tests written first and observed failing (Gate 2)
- [ ] `preflight.sh --full` green
- [ ] Coverage floors not lowered
- [ ] Playwright covers the login desk
- [ ] Permanent documentation updated (Gate 3)
- [ ] Commit references #2069 with `Refs`
- [ ] PR opened
- [ ] Merge, deploy, and smoke remain after review
- [ ] `traceability.md` updated through the PR
