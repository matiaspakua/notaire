> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1942 linked to RNF-09
- [x] 1.2 Use Case RNF-09 exists
- [x] 1.3 Acceptance Criteria in delta spec
- [x] 1.4 Impact Analysis in proposal.md
- [x] 1.5 No ADR required
- [x] 1.6 Issue claimed (`claim-issue.py`)

## 2. Crear branch

- [x] 2.1 Updated `main`
- [x] 2.2 `feat/1942_page-chrome-primitives`
- [x] 2.3 Branch recorded in traceability

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate scenarios
- [ ] 3.2 Write `page-chrome.test.tsx`
- [ ] 3.3 n/a backend
- [ ] 3.4 Observe Vitest fail
- [ ] 3.5 Map every Scenario to a test

## 4. Implementación

- [ ] 4.1 Implement PageHeader, EmptyState, PageSection
- [ ] 4.2 Wire AppHeader + DataTable + dashboard / people / managements pilots
- [ ] 4.3 Drop ALL-CAPS on DataTable headers

## 5. Actualizar tests existentes

- [ ] 5.1 Keep dashboard/page tests green
- [ ] 5.2 No weakened assertions
- [ ] 5.3 n/a obsolete

## 6. Ejecutar regresión

- [ ] 6.1–6.4 n/a backend / Bruno
- [ ] 6.5 `bash frontend/verify.sh` (or vitest + typecheck + eslint)

## 7. Ejecutar Playwright

- [ ] 7.1 Rely on path-scoped CI Playwright; spot-check if stack up
- [ ] 7.2 Preserve people/managements testids
- [ ] 7.3 Viewport spot-check
- [ ] 7.4 UI surface present

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 FRONTEND-DESIGN-SYSTEM.md + CHANGELOG
- [ ] 8.2 n/a OpenAPI
- [ ] 8.3 CHANGELOG entry
- [ ] 8.4 n/a archive docs
- [ ] 8.5 No duplication
- [ ] 8.6 preflight before push

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits
- [ ] 9.2 PR body `Closes #1942`
- [ ] 9.3 No unrelated skill edits
- [ ] 9.4 Record SHAs

## 10. Pull Request y validación CI

- [ ] 10.1 Archive OpenSpec in PR
- [ ] 10.2 Push
- [ ] 10.3 Open PR
- [ ] 10.4 CI green
- [ ] 10.5 Gate 4

## 11. Deploy

- [ ] 11.1 Merge via PR

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke dashboard + people
- [ ] 12.2 Issue closed via Closes

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
