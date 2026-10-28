> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural; no ADR (Mermaid stays per ADR-027; inline SVG like `GateFlow`)
- [x] 1.6 Move the Issue to IN PROGRESS (`status:in-progress`) — label API refused (403); noted in the PR

## 2. Crear branch

- [x] 2.1 Worktree from updated `origin/main`
- [x] 2.2 `docs/1970_pages_ux_redesign`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write the unit tests for every scenario in the delta spec
- [x] 3.3 Component tests (Vitest, Testing Library) for the header, drawer and search; Playwright smoke in `github-page/e2e/`
- [x] 3.4 Observed failing: no `SiteHeader`, no `DocShell`, no results page, no Pages CI
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

<!-- Change-specific work goes here. Break it into small, verifiable tasks ordered
     by dependency. Write only the code needed to make the tests of group 3 pass. -->

- [x] 4.1 UX assessment at 375/768/1280 px with axe: `github-page/docs/ux-assessment.md`
- [x] 4.2 Site map `lib/site.ts` (sections, five acts, search index, breadcrumbs, previous/next)
- [x] 4.3 Design tokens in `app/globals.css`; fonts; remove the custom cursor
- [x] 4.4 `components/site/`: `SiteHeader` (drawer, focus trap, search), `SiteFooter`, `DocShell`, `Breadcrumbs`, `Sidebar`, `PrevNext`, `ReadingProgress`
- [x] 4.5 Replace `Nav`, `FactoryChrome`, `DocsChrome`, `Footer`; per-page metadata
- [x] 4.6 Mermaid theme from tokens, `role="img"`, mobile scroll; `GuardrailStack` and `Swimlane` SVG diagrams
- [x] 4.7 AI Factory story: overview problem and act map, `/ai-sdlc/results/`, contrast and overflow fixes
- [x] 4.8 Tooling: ESLint, Vitest, Playwright, link checker, `pages-ci.yml`, `preflight.sh`

## 5. Actualizar tests existentes

- [x] 5.1 Affected: `test_pages_ai_sdlc.py` (nav now in `SiteHeader`/`site.ts`), `test_pages_business_docs.py` (chrome now `lib/site.ts`)
- [x] 5.2 Updated to the new file that owns the behaviour; assertions kept or added (results, SVG diagrams, Mermaid theme)
- [x] 5.3 None removed

## 6. Ejecutar regresión

- [x] 6.1 n/a — no backend change; workspace suites run instead
- [x] 6.2 n/a — no backend change
- [x] 6.3 n/a — no backend change
- [x] 6.4 n/a — no backend change
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI change; `testing/e2e` does not cover the Pages site
- [x] 7.2 `github-page` Playwright smoke (`npm run test:smoke`) green at 375 and 1280 px
- [x] 7.3 Before/after screenshots at 375 and 1280 px
- [x] 7.4 axe: no WCAG A/AA violations on the audited pages

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 n/a — no endpoint change
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.4 n/a — nothing superseded
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #<issue-number>`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [x] 10.2 `git push -u origin <branch-name>`
- [x] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [x] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [x] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [x] 12.2 Verify the rollback path is still available as described in design.md
- [x] 12.3 Close the GitHub Issue, referencing the PR
- [x] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [x] PR created, CI green, review approved (Gate 4)
- [x] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [x] `traceability.md` complete from Issue through Release
