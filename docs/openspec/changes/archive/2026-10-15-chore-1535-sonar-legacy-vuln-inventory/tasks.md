> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.


## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit <n> --add-label "in-progress"`) — prefer comment + Refs; do not claim exclusive ownership of umbrella close

## 2. Create branch

- [x] 2.1 `git checkout main && git pull origin main` — **only after #1755 merges**
- [x] 2.2 `git checkout -b cursor/chore-1535-sonar-legacy-vuln-inventory-2ea7`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths
- [x] 3.2 Write unit tests for inventory JSON parsing / markdown emission (fixture-based)
- [x] 3.3 Write integration tests where applicable — n/a live Sonar in CI
- [x] 3.4 Run them and **observe them fail**
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementation

- [x] 4.1 Add `infra/scripts/sonar-legacy-inventory.py` (English identifiers)
- [x] 4.2 Emit / update `docs/300-development/SONAR-LEGACY-VULN-INVENTORY.md` with classifications
- [x] 4.3 Apply only safe non-hexagon `fix` patches with tests (optional in this PR)
- [x] 4.4 Update `.claude/rules/code-quality.md` pointer + CHANGELOG Unreleased

## 5. Update existing tests

- [x] 5.1 Identify existing tests affected (severity report stays green)
- [x] 5.2 Update them without weakening assertions
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Run regression

- [x] 6.1 `mvn test -pl backend-api` — if backend touched; else n/a
- [x] 6.2 `mvn jacoco:check -pl backend-api` — if backend touched; else n/a
- [x] 6.3 `mvn verify -pl backend-api` — if backend touched; else n/a
- [x] 6.4 Bruno / integration — n/a unless API contract changes
- [x] 6.5 No `@Disabled` or skipped tests without documented justification

## 7. Run Playwright

- [x] 7.1 Add/update E2E specs — n/a
- [x] 7.2 Playwright suite — n/a
- [x] 7.3 Responsive checks — n/a
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 Update permanent docs listed in proposal.md
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` after #1755 / no competing Changed lock
- [x] 8.4 Archive superseded docs if any
- [x] 8.5 No duplicated sources of truth
- [x] 8.6 `bash workspace/sdlc/preflight.sh --fix` / CI

## 9. Atomic commits

- [x] 9.1 Conventional Commits; English messages
- [x] 9.2 Use `Refs #1535` for slice 1 (prefer not `Closes` until umbrella AC done)
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request and CI validation

- [x] 10.1 Archive OpenSpec change in the PR
- [x] 10.2 `git push -u origin cursor/chore-1535-sonar-legacy-vuln-inventory-2ea7`
- [x] 10.3 Open PR `[#1535] chore(quality): inventory and classify legacy Sonar vulnerabilities`
- [ ] 10.4 Wait for required workflows
- [ ] 10.5 Gate 4 — CI green, heavy-CI exit 0, no conflicts
- [ ] 10.6 Record PR number in archived `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only
- [ ] 11.2 Confirm CD if applicable
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test and close

- [ ] 12.1 Local/Mac: `python3 infra/scripts/sonar-severity-report.py` + refresh inventory when Sonar up
- [ ] 12.2 Rollback path available (revert PR)
- [ ] 12.3 Comment progress on #1535; do **not** close umbrella unless AC complete
- [ ] 12.4 Record evidence in archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria (slice scenarios)
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green for touched modules
- [x] Coverage at or above the JaCoCo ratchet floor (if backend touched)
- [x] Playwright E2E n/a
- [x] Permanent documentation updated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged; umbrella #1535 updated with counts (Gate 5 partial)
- [ ] `traceability.md` complete for this slice
