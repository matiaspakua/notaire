> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1041 OPEN; CU76; labels DEVOPS/priority:high/ci/audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update notes at implement if needed
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — update ADR-012 text only (no new ADR number required)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1041 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1042 merges** (queue: #1051 → #1046 → #1042 → #1041)
- [x] 2.2 `git checkout -b cursor/fix-1041-no-bot-commits-main-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1041/` into `openspec/changes/fix-1041-no-bot-commits-main/` and run `bash scripts/validate-sdlc-plan.sh fix-1041-no-bot-commits-main`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/ci-no-bot-report-commits/spec.md` (no commits in four workflows; artifact/summary/Pages channel; ignore+untrack; drop `contents: write` on report jobs)
- [x] 3.2 Write failing `scripts/test_no_bot_report_commits.py` asserting current `origin/main` still has commit/push steps and `contents: write` on `publish-reports` / `publish-report` / `coverage-report`, and that `docs/wiki/cicd-reports/` is tracked / not ignored
- [x] 3.3 Run them and **observe them fail** on the pre-change tree (`python3 scripts/test_no_bot_report_commits.py`)
- [x] 3.4 Confirm every `#### Scenario:` in the delta spec maps to at least one test or explicit checklist item
- [x] 3.5 Integration tests — n/a for workflow YAML (document n/a)

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: `ci.yml`/`cd.yml`/`playwright-e2e.yml` still commit reports; `pr-validation.yml` still does not; CD `release` still needs `contents: write`
- [x] 4.2 Remove git commit/push (and wiki-stage-only) steps from `ci.yml` `publish-reports`; keep/add artifact upload + `$GITHUB_STEP_SUMMARY`; drop job `contents: write`
- [x] 4.3 Same for `cd.yml` `publish-report`; do not weaken `release` permissions
- [x] 4.4 Same for `playwright-e2e.yml` `coverage-report` (generate markdown → artifact/summary, no push)
- [x] 4.5 Re-assert `pr-validation.yml` stays commit-free for wiki reports
- [x] 4.6 Add `docs/wiki/cicd-reports/` to `.gitignore`; remove tracked files under that path from the index/tree
- [x] 4.7 Optional same-PR: surface latest markdown via `deploy-github-page.yml` download into `github-page/public/` (no git commit) if cheap; otherwise document artifacts+summary as AC-compliant
- [x] 4.8 Make `test_no_bot_report_commits.py` pass without weakening AC asserts; update `test_ci_workflow_invariants.py` narrative/asserts that assumed wiki commits

## 5. Actualizar tests existentes

- [x] 5.1 Identify scripts that assume wiki commits (`test_ci_workflow_invariants.py`, report `needs` tests, any PR-checks fixtures mentioning bot report commits)
- [x] 5.2 Update them to expect artifact/summary publish without weakening dependency or Pages invariants
- [x] 5.3 Remove tests made genuinely obsolete only if they asserted “must commit to wiki” — state the reason

## 6. Ejecutar regresión

- [x] 6.1 `python3 scripts/test_no_bot_report_commits.py` — green
- [x] 6.2 `python3 scripts/test_ci_workflow_invariants.py` (+ related workflow unit tests) — green
- [x] 6.3 Backend `mvn verify` — n/a for YAML/docs (note); still run heavy gate on PR
- [x] 6.4 HTTP/Bruno — n/a for API delta
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (workflow + ignore + docs); `playwright-e2e.yml` job shape changes only for report publish
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (do not skip)
- [x] 7.3 Serialize if other Playwright-heavy PRs touch the same workflow file
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability with the reason above

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for no-bot-report-commits
- [x] 8.4 Archive superseded documents into `docs/000-archive/` only if a live doc is replaced
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. `fix(ci): stop committing wiki reports to main`, `test: …`, `docs: …`, `chore: untrack cicd-reports`)
- [ ] 9.2 Every commit message ends with `Closes #1041`
- [ ] 9.3 No secrets, no commented-out code, no unrelated product rewrites
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1041-no-bot-commits-main-69d3`
- [ ] 10.2 Open the PR titled `[#1041] fix(ci): stop CI bots committing reports to main`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0)
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the next CI/CD/E2E runs on `main` do **not** create `CI Bot` “docs: add … report” commits
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: post-merge CI/CD/E2E runs show report artifacts and/or step summary (and Pages if wired); no new report commits on `main`; report jobs lack `contents: write`
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1041-no-bot-commits-main`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; unit tests written first and observed failing (Gate 2)
- [ ] Implementation passes the applicable suite; heavy CI green (Gate 3/4)
- [ ] Coverage gate unaffected / held (no Java delta)
- [ ] Playwright: n/a product E2E; PR Playwright job still green
- [ ] Permanent documentation updated and consistent (no duplication)
- [ ] Commits atomic, Conventional Commits, `Closes #1041`
- [ ] Pull Request created, CI/CD green, code review approved (Gate 4)
- [ ] Merged to `main` via PR; no new bot report commits observed; Issue closed (Gate 5)
