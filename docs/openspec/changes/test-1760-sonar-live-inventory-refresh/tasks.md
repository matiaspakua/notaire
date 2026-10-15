> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.


## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists (#1760), linked to CU78 / CU76
- [x] 1.2 Use Case documentation exists (CU78)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (no architectural change)
- [x] 1.6 Issue marked in progress (comment on #1760)

## 2. Create branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/1760-sonar-live-inventory-refresh-69d3`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases: CI secret heuristic, MD5 fix heuristic, OTLP accepted_risk, live refresh note
- [x] 3.2 Write unit tests in `infra/tests/test_sonar_legacy_inventory.py`
- [x] 3.3 Integration — live Sonar via `run-sonar.sh` on Mac (not in CI)
- [x] 3.4 Observed failing: empty classification / missing `source_note` kwarg
- [x] 3.5 Every `#### Scenario:` maps to a unit test

## 4. Implementation

- [x] 4.1 `bash infra/scripts/run-sonar.sh` (backend + frontend) against localhost:9000
- [x] 4.2 Heuristics + `Last live refresh` in `sonar-legacy-inventory.py`
- [x] 4.3 `python3 infra/scripts/sonar-legacy-inventory.py --write` (702 live rows; 5 VULNERABILITY)
- [x] 4.4 Child issue #1762 for `java:S4790` FIX cluster
- [x] 4.5 `run-sonar.sh` Docker network auto-detect
- [x] 4.6 CHANGELOG skipped (CHANGELOG-free guidance)

## 5. Update existing tests

- [x] 5.1 Existing inventory tests remain green
- [x] 5.2 Assertions extended for #1762 rationale citation
- [x] 5.3 No obsolete tests removed

## 6. Run regression

- [x] 6.1 `python3 -m unittest infra.tests.test_sonar_legacy_inventory` — OK
- [x] 6.2 Backend Maven / JaCoCo — exercised by live `run-sonar.sh` (QG PASSED)
- [x] 6.3 SpotBugs/Checkstyle — not changed by this PR
- [x] 6.4 Bruno — n/a (no API change)
- [x] 6.5 No `@Disabled` introduced

## 7. Run Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a
- [x] 7.3 n/a
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 `SONAR-LEGACY-VULN-INVENTORY.md` regenerated from live Sonar
- [x] 8.2 OpenAPI — n/a
- [x] 8.3 `CHANGELOG.md` — n/a (CHANGELOG-free)
- [x] 8.4 No archives superseded
- [x] 8.5 Single inventory source of truth retained
- [ ] 8.6 `bash workspace/sdlc/preflight.sh` before push

## 9. Atomic commits

- [ ] 9.1 Conventional Commits; English messages
- [ ] 9.2 `Closes #1760` / `Refs #1535` / `Refs #1762`
- [ ] 9.3 No secrets committed (`.env` token stays local)
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request and CI validation

- [ ] 10.1 Archive OpenSpec change in the PR (after merge / with PR)
- [ ] 10.2 `git push -u origin cursor/1760-sonar-live-inventory-refresh-69d3`
- [ ] 10.3 Open PR for #1760
- [ ] 10.4 Wait for required workflows
- [ ] 10.5 Gate 4 — `bash workspace/sdlc/check-heavy-ci.sh <pr>` exits 0
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via PR only (after heavy-CI 0)
- [ ] 11.2 CD — n/a for docs/scripts
- [ ] 11.3 Record merge commit in `traceability.md`

## 12. Gate 5 — Smoke test and close

- [ ] 12.1 Confirm inventory on tip main; comment on #1535 with VULNERABILITY counts
- [ ] 12.2 Rollback = revert PR
- [ ] 12.3 Close #1760 via PR; do **not** reopen #1740; leave #1535 / #1762 open
- [ ] 12.4 Record evidence in archived `traceability.md`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Touched-module tests green
- [x] Playwright E2E n/a
- [x] Permanent inventory updated (Gate 3)
- [ ] Commits atomic and conventional
- [ ] PR created, heavy-CI green (Gate 4)
- [ ] Merged; #1760 closed; #1535 updated (Gate 5)
