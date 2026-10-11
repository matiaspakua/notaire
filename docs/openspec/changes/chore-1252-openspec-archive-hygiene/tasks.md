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
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1252 --add-label "in-progress"`) — may 403 for integration token; record in Exceptions if so

## 2. Crear branch

- [x] 2.1 `git fetch origin main` and update local main
- [x] 2.2 Branch `cursor/chore-1252-openspec-archive-hygiene-69d3` (fleet suffix)
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate cases: closed-issue active change; archived pass; open WIP; unticked 3–8 fail; ticked 3–8 pass; sdlc-exception bypass; merge-gate doc string
- [ ] 3.2 Write `workspace/tests/test_openspec_archive_hygiene.py` (or equivalent) for every scenario
- [ ] 3.3 No Java integration tests — n/a
- [ ] 3.4 Run the new tests and **observe them fail** before the checker exists
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [ ] 4.1 Add `workspace/sdlc/` checker for archive inventory + task groups 3–8 on touched changes
- [ ] 4.2 Wire checker into Process Checks / `preflight.sh` (same family as other SDLC scripts)
- [ ] 4.3 Update `CI-MERGE-GATE.md` (+ fleet README / FLEET-ARCHITECTURE as listed in proposal)
- [ ] 4.4 CU76 short pointer to OpenSpec lifecycle / archive rule
- [ ] 4.5 Re-inventory active changes; archive shipped (sync specs when needed); triage orphans
- [ ] 4.6 Do not edit `CHANGELOG.md` until #1518 merges; follow-up Unreleased note afterward if still required

## 5. Actualizar tests existentes

- [ ] 5.1 Confirm `test_docs_business_pages_link.py` still passes after archive moves
- [ ] 5.2 Confirm `validate-sdlc-plan.sh` structure checks unchanged for other active changes
- [ ] 5.3 Remove nothing unless a guard genuinely becomes obsolete — document why

## 6. Ejecutar regresión

- [ ] 6.1 New unittest module green
- [ ] 6.2 `bash workspace/sdlc/validate-sdlc-plan.sh chore-1252-openspec-archive-hygiene` green
- [ ] 6.3 `openspec validate chore-1252-openspec-archive-hygiene --strict` green
- [ ] 6.4 `bash workspace/sdlc/preflight.sh` (or docs-only subset) as applicable — no Java product change
- [ ] 6.5 No `@Disabled` / skipped tests without documented justification

## 7. Ejecutar Playwright

- [ ] 7.1 n/a — no UI surface (process + docs only)
- [ ] 7.2 Record n/a in PR body
- [ ] 7.3 Viewports n/a
- [ ] 7.4 Explicit: no Playwright specs required for SURFACE=none

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact (except CHANGELOG while #1518 holds it)
- [ ] 8.2 OpenAPI n/a — no endpoints
- [ ] 8.3 CHANGELOG deferred — Related #1518 serialization
- [ ] 8.4 Archive superseded OpenSpec active trees into `docs/openspec/changes/archive/`
- [ ] 8.5 Confirm no duplicated process prose — cite CONSTITUTION / CI-MERGE-GATE
- [ ] 8.6 Preflight / process self-tests discover the new unittest

## 9. Commits atómicos

- [ ] 9.1 Conventional Commits; Gate 1 pack separate from implement/archive commits when useful
- [ ] 9.2 Closing commit/PR uses `Closes #1252` only when AC are fully met; otherwise `Related: #1252` / `Refs: #1252`
- [ ] 9.3 No secrets, no unrelated product edits, no CHANGELOG until cleared
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/chore-1252-openspec-archive-hygiene-69d3`
- [ ] 10.2 Open PR referencing #1252 and CU76 (draft OK for Gate 1-only pack)
- [ ] 10.3 Wait for required workflows; docs/process PRs may path-skip heavy product jobs
- [ ] 10.4 Gate 4 — CI green, review, no conflicts, docs complete for the landed slice
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via pull request only
- [ ] 11.2 No container image contract change
- [ ] 11.3 Smoke: process checker exit codes on fixture cases after merge

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 After merge, active-set inventory no longer lists shipped #1467 (and other closed-issue leftovers found at apply time)
- [ ] 12.2 Revert of the checker is safe (no schema)
- [ ] 12.3 Close #1252 only when all Acceptance Criteria pass
- [ ] 12.4 Archive `chore-1252-openspec-archive-hygiene` and sync `ai-sdlc-enforcement` main spec

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1 artifacts present)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Process checker + docs + archive reconciliation landed
- [ ] Coverage ratchet unchanged — no Java edit
- [ ] Playwright not applicable
- [ ] Permanent documentation updated (Gate 3) except CHANGELOG serialization
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
