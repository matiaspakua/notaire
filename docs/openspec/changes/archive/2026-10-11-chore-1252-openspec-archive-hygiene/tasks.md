> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1252 exists, labeled DEVOPS/DOC, linked to CU76
- [x] 1.2 Use Case CU76 exists under `docs/100-business/102-use-cases/`
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/ai-sdlc-enforcement/spec.md`
- [x] 1.4 Impact Analysis and SURFACE=none confirmed in `proposal.md`
- [x] 1.5 No ADR required — process hygiene, not architectural
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1252 --add-label "in-progress"`) — 403 for integration token; recorded in Exceptions

## 2. Create branch

- [x] 2.1 `git fetch origin main` and update local main
- [x] 2.2 Branch `cursor/chore-1252-openspec-archive-hygiene-69d3` (fleet suffix)
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Write tests (TDD, failing first)

- [x] 3.1 Enumerate cases: closed-issue active change; archived pass; open WIP; unticked 3–8 fail; ticked 3–8 pass; sdlc-exception bypass; merge-gate doc string
- [x] 3.2 Write `workspace/tests/test_openspec_archive_hygiene.py` (or equivalent) for every scenario
- [x] 3.3 No Java integration tests — n/a
- [x] 3.4 Run the new tests and **observe them fail** before the checker exists
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementation

- [x] 4.1 Add `workspace/sdlc/` checker for archive inventory + task groups 3–8 on touched changes
- [x] 4.2 Wire checker into Process Checks / `preflight.sh` (same family as other SDLC scripts)
- [x] 4.3 Update `CI-MERGE-GATE.md` (+ fleet README / FLEET-ARCHITECTURE as listed in proposal)
- [x] 4.4 CU76 short pointer to OpenSpec lifecycle / archive rule
- [x] 4.5 Re-inventory active changes; archive shipped (sync specs when needed); triage orphans
- [x] 4.6 Do not edit `CHANGELOG.md` — held for this PR per foreman serialize (follow-up Unreleased note allowed later)

## 5. Update existing tests

- [x] 5.1 Confirm `test_docs_business_pages_link.py` still passes after archive moves
- [x] 5.2 Confirm `validate-sdlc-plan.sh` structure checks unchanged for other active changes
- [x] 5.3 Remove nothing unless a guard genuinely becomes obsolete — document why

## 6. Run regression

- [x] 6.1 New unittest module green
- [x] 6.2 `bash workspace/sdlc/validate-sdlc-plan.sh chore-1252-openspec-archive-hygiene` green
- [x] 6.3 `openspec validate chore-1252-openspec-archive-hygiene --strict` green
- [x] 6.4 `bash workspace/sdlc/preflight.sh` (or docs-only subset) as applicable — no Java product change
- [x] 6.5 No `@Disabled` / skipped tests without documented justification

## 7. Run Playwright

- [x] 7.1 n/a — no UI surface (process + docs only)
- [x] 7.2 Record n/a in PR body
- [x] 7.3 Viewports n/a
- [x] 7.4 Explicit: no Playwright specs required for SURFACE=none

## 8. Gate 3 — Update permanent documentation

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact (except CHANGELOG — held)
- [x] 8.2 OpenAPI n/a — no endpoints
- [x] 8.3 CHANGELOG deferred / held for this PR
- [x] 8.4 Archive superseded OpenSpec active trees into `docs/openspec/changes/archive/`
- [x] 8.5 Confirm no duplicated process prose — cite CONSTITUTION / CI-MERGE-GATE
- [x] 8.6 Preflight / process self-tests discover the new unittest

## 9. Atomic commits

- [x] 9.1 Conventional Commits; Gate 1 pack separate from implement/archive commits when useful
- [x] 9.2 Closing commit/PR uses `Closes #1252` only when AC are fully met; otherwise `Related: #1252` / `Refs: #1252`
- [x] 9.3 No secrets, no unrelated product edits, no CHANGELOG until cleared
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request and CI validation

- [x] 10.1 `git push -u origin cursor/chore-1252-openspec-archive-hygiene-69d3`
- [x] 10.2 Open PR referencing #1252 and CU76 (draft OK for Gate 1-only pack)
- [ ] 10.3 Wait for required workflows; docs/process PRs may path-skip heavy product jobs
- [ ] 10.4 Gate 4 — CI green, review, no conflicts, docs complete for the landed slice
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via pull request only
- [ ] 11.2 No container image contract change
- [ ] 11.3 Smoke: process checker exit codes on fixture cases after merge

## 12. Gate 5 — Smoke test and close

- [ ] 12.1 After merge, active-set inventory no longer lists shipped leftovers found at apply time
- [ ] 12.2 Revert of the checker is safe (no schema)
- [ ] 12.3 Close #1252 only when all Acceptance Criteria pass
- [ ] 12.4 Archive `chore-1252-openspec-archive-hygiene` and sync `ai-sdlc-enforcement` main spec

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1 artifacts present)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Process checker + docs + archive reconciliation landed
- [x] Coverage ratchet unchanged — no Java edit
- [x] Playwright not applicable
- [x] Permanent documentation updated (Gate 3) except CHANGELOG serialization
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
