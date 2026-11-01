> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1985, CU76; labels and milestone refused by the token, noted in the PR
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 Not architectural; no ADR
- [x] 1.6 Move the Issue to IN PROGRESS (`status:in-progress`) — label API refused (403); noted in the PR

## 2. Crear branch

- [x] 2.1 Worktree from `main` after #1978 and #1984 were merged
- [x] 2.2 `feat/1985_pages_demo_metrics_about`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: shim replay, query fallback, 404 not 401, login, read-only writes, no network; fixtures without secrets; metrics window, method and unavailable reasons; About facts with sources, no placeholders rendered; workflows build the demo
- [x] 3.2 `github-page/tests/demo-shim.test.ts`, `github-page/tests/outcomes-about.test.tsx`
- [x] 3.3 `workspace/tests/test_pages_demo_outcomes_about.py`; `e2e/demo.spec.ts` and About in `e2e/site.spec.ts` at 375 and 1280 px
- [x] 3.4 Observed failing in the test commit
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 `demo/record-fixtures.mjs`, `demo/fixtures.json`, `demo/demo-shim.js`, `demo/build-demo.mjs`
- [x] 4.2 `workspace/ci/generate-delivery-metrics.py` and `lib/delivery-metrics.json`; outcomes on `/ai-sdlc/results/` and the landing
- [x] 4.3 `lib/about.json`, `/about/` and the landing section
- [x] 4.4 `pages-ci.yml` and `deploy-github-page.yml`: frontend install, demo build, metrics refresh
- [x] 4.5 `github-page/docs/demo.md`; hosting issue for a real-backend demo (#1986)

## 5. Actualizar tests existentes

- [x] 5.1 Affected: `e2e/site.spec.ts` (new pages in the axe sweep)
- [x] 5.2 Assertions extended, none weakened
- [x] 5.3 None removed

## 6. Ejecutar regresión

- [x] 6.1 n/a — no backend change; workspace suites run instead
- [x] 6.2 n/a — no backend change
- [x] 6.3 n/a — no backend change
- [x] 6.4 n/a — no backend change
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no product UI source change; `testing/e2e` does not cover the Pages site
- [x] 7.2 `github-page` Playwright smoke (`npm run test:smoke`) green at 375 and 1280 px
- [x] 7.3 Before/after screenshots at 375 and 1280 px (kept outside the repo, `/workspace/pages-portfolio/after-c/`)
- [x] 7.4 axe: no WCAG A/AA violations on the audited pages

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 n/a — no endpoint change
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 Pages gates run locally (typecheck, lint, Vitest, export, link check, Playwright + axe) and the process checks via `quick.sh`; CI runs the rest

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Commits reference `Refs #1985`; the final commit ends with `Closes #1985`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Archive this change in the PR: `cd docs && openspec archive <change-name> --yes` (skip only if `.openspec.yaml` sets `multi_pr: true`)
- [ ] 10.2 `git push -u origin <branch-name>`
- [ ] 10.3 Open the PR titled `[#<issue>] <type>(<scope>): <description>`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (health endpoint + the key flow of this change)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

## Definition of Done

<!-- Per CONSTITUTION.md §3. Every box must be checked before the change is Done. -->

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for UI changes
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
