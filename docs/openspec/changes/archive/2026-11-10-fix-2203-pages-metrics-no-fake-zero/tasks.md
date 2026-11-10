> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [ ] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [ ] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a, honesty fix on existing generator
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — if labels/assignees are denied by token, record in PR body

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b cursor/fix-2203-pages-metrics-no-fake-zero-fc7d`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md Testing Strategy)
- [ ] 3.2 Write `workspace/tests/test_generate_github_page_metrics.py` for every generator scenario in the delta spec
- [ ] 3.3 Write `github-page/tests/metrics-coverage-honesty.test.ts` for null/unknown UI honesty
- [ ] 3.4 Run them and **observe them fail** before implementing the script/UI fix
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Patch `workspace/ci/generate-github-page-metrics.sh` — null defaults; refuse poisoned live zeros when `lastUpdated` is unknown; keep real snapshot/summary updates and measured live reuse
- [ ] 4.2 Update `github-page/lib/metrics.ts` (+ consumers/copy) so `number | null` and `coverageIsMeasured` never present unmeasured fields as `0%`
- [ ] 4.3 Regenerate `github-page/public/metrics.json` via the shared script; update `CHANGELOG.md` and `github-page/README.md`; Closes #2203

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected (`metrics-test-counts.test.ts`, any Numbers/Results assertions)
- [ ] 5.2 Update them without weakening assertions; document why any old expectation was wrong
- [ ] 5.3 Remove tests made genuinely obsolete, stating the reason — or n/a

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — n/a, no Java change (record n/a)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [ ] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 `bash integration-test/scripts/test.sh` — n/a, no API surface
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the E2E specs listed in design.md — Playwright Strategy — n/a
- [ ] 7.2 `cd testing/e2e && npx playwright test` — n/a for product E2E; run `github-page` Vitest instead
- [ ] 7.3 Verify the affected screens at 320px, 768px and 1024px — n/a product UI
- [ ] 7.4 If the change has no UI surface, record "n/a — no product UI surface; portfolio covered by Vitest"

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed — n/a
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible changes
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — n/a
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash workspace/sdlc/preflight.sh --full` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #<issue-number>` — use `Closes #2203`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 Archive this change in the PR: `cd docs && openspec archive fix-2203-pages-metrics-no-fake-zero --yes`
- [ ] 10.2 `git push -u origin cursor/fix-2203-pages-metrics-no-fake-zero-fc7d`
- [ ] 10.3 Open the PR titled `[#2203] fix(pages): do not publish 0% coverage when JaCoCo snapshot missing`, referencing Issue and Use Case
- [ ] 10.4 Wait for every required workflow to pass (release-only heavy CI may path-skip; local `--full` is the PR proof per #2001)
- [ ] 10.5 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.6 Record the PR number in the archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD / Pages deploy republished metrics — or n/a until merge
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (live `/metrics.json` honesty check)
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR — `Closes #2203` on merge
- [ ] 12.4 Record deploy and smoke evidence in the archived `traceability.md`

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
