> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case — #960 OPEN; CU76 / RF #78; FRONTEND / tech-debt
- [x] 1.2 Use Case documentation exists — CU76 permanent doc; RF #78 in traceability matrix
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (design-system compliance, not architectural)
- [ ] 1.6 Move the Issue to IN PROGRESS — attempted; worker `gh` lacks label write (coordinator)

## 2. Crear branch

- [x] 2.1 `git fetch origin main`
- [x] 2.2 `git checkout -b cursor/style-960-dashboard-theme-tokens-69d3 origin/main`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate cases: layout/page/table hex-free; audited globs; issue-ref exclusion
- [x] 3.2 Write failing `frontend/src/tests/unit/hex-hygiene.test.ts`
- [x] 3.3 Integration tests — n/a (frontend style hygiene)
- [x] 3.4 Observe Vitest fail on tip (`npm test -- hex-hygiene`) — red logged
- [x] 3.5 Map every `#### Scenario:` to a test or viewport evidence

## 4. Implementación

- [x] 4.1 Replace hex in `dashboard/layout.tsx` with theme token / semantic class; Englishize touched aria-label
- [x] 4.2 Replace hex in `dashboard/page.tsx` with theme tokens / semantic Tailwind
- [x] 4.3 Replace hex in `components/ui/table.tsx`; preserve header/hover opacity via token-based color-mix/opacity
- [x] 4.4 Confirm hygiene test green; no new hex invented outside `tokens.ts`

## 5. Actualizar tests existentes

- [x] 5.1 Identify affected tests (data-table, pages, dashboard-nav) — none broken
- [x] 5.2 Update only if assertions depended on hex class strings — n/a
- [x] 5.3 Remove obsolete tests only with stated reason — none

## 6. Ejecutar regresión

- [x] 6.1 Backend Maven — n/a delta (frontend-only)
- [x] 6.2 JaCoCo — n/a delta
- [x] 6.3 `cd frontend && npm test && npm run lint && npm run typecheck` — green (369 tests)
- [x] 6.4 Bruno — n/a
- [x] 6.5 No unjustified skips

## 7. Ejecutar Playwright

- [x] 7.1 Prefer cheap smoke or computerUse viewport evidence at 320/768/1024 — Playwright screenshots
- [x] 7.2 Heavy CI Playwright may run — keep diff style-only
- [x] 7.3 Verify dashboard appearance at 320 / 768 / 1024 — evidence under `/opt/cursor/artifacts/dashboard-960-*.png`
- [x] 7.4 Serialize awareness vs other Playwright-heavy PRs — **not n/a**

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update docs listed in proposal Documentation Impact
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` Changed entry for #960
- [x] 8.4 Archive only if a doc becomes obsolete
- [x] 8.5 No duplicated SSOT content
- [x] 8.6 `bash scripts/preflight.sh` — frontend/backend gates green; repo-wide SDLC validation fails on unrelated CLOSED-issue changes (ours alone PASS)

## 9. Commits atómicos

- [x] 9.1 Conventional Commits, small units
- [x] 9.2 Every commit ends with `Closes #960`
- [x] 9.3 No secrets / unrelated changes
- [x] 9.4 Record SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Push branch
- [x] 10.2 PR title `[#960] style(frontend): dashboard/table theme tokens`
- [ ] 10.3 Wait for required workflows including Playwright as applicable
- [ ] 10.4 Merge only on heavy-CI gate exit 0 (coordinator)
- [x] 10.5 Record PR in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm frontend CD as applicable
- [ ] 11.3 Record merge commit / tag

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: dashboard home + table colors/hover look correct
- [ ] 12.2 Rollback path available per design.md
- [ ] 12.3 Close Issue
- [ ] 12.4 `openspec archive style-960-dashboard-theme-tokens`

## Definition of Done

- [ ] Issue linked to Use Case with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green
- [ ] Coverage floors held
- [ ] Playwright E2E green for UI changes (or documented viewport evidence + heavy CI)
- [ ] Permanent documentation updated
- [ ] Commits atomic and conventional
- [ ] PR created, CI green, review approved
- [ ] Merged, smoke passed, Issue closed
- [ ] `traceability.md` complete
