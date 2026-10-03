> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1040 OPEN; CU76 + CU78; labels DEVOPS/priority:critical/security/audit-2026-09
- [x] 1.2 Use Case documentation exists and is accurate — CU76 and CU78 docs exist under `docs/100-business/102-use-cases/`
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — no new ADR number; update DevSecOps / existing CI ADR text only
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1040 --add-label "in-progress"`) — attempted; GraphQL ACL 403 for integration (recorded)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after #1041 merge tip `6b246a72`
- [x] 2.2 `git checkout -b cursor/ci-1040-protect-main-ruleset-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1040/` into `openspec/changes/ci-1040-protect-main-ruleset/` and run `bash scripts/validate-sdlc-plan.sh ci-1040-protect-main-ruleset`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from `specs/protect-main-ruleset/spec.md` (PR rule; five exact checks; deletion/non-FF; no durable bypass; hooks.md; aggregators)
- [x] 3.2 Write failing `scripts/test_protect_main_ruleset.py` asserting: desired JSON / workflows / hooks.md
- [x] 3.3 Run them and **observe them fail** on the pre-change tree (`python3 scripts/test_protect_main_ruleset.py` → FAILED)
- [x] 3.4 Confirm every `#### Scenario:` in the delta spec maps to at least one test or Gate 5 assert checklist item
- [x] 3.5 Integration tests — n/a for Maven; live `gh api` assert is Gate 5 (`assert-protect-main-ruleset.sh`)

## 4. Implementación

- [x] 4.1 Re-verify on updated `main`: ruleset `24128115` still only deletion + non_fast_forward; #1041 merged; bypass empty; hooks.md gap present until this PR
- [x] 4.2 Add aggregator jobs with exact `name:` values `CI`, `Frontend CI`, `Playwright E2E`, `PR Validation`; keep `Code Lint`
- [x] 4.3 Add `scripts/rulesets/protect-main.desired.json` with PR + five checks + deletion + non_fast_forward; **bypass_actors empty**
- [x] 4.4 Add `scripts/apply-protect-main-ruleset.sh` (dry-run default, `--apply` admin) and `scripts/assert-protect-main-ruleset.sh`
- [x] 4.5 `test_protect_main_ruleset.py` green for in-repo artifacts; live assert fails until admin `--apply` (expected)
- [x] 4.6 Update `.claude/rules/hooks.md` (protect-main + hook defense-in-depth)
- [ ] 4.7 After PR checks green: admin runs apply; run assert; bypass remains empty

## 5. Actualizar tests existentes

- [x] 5.1 Identified workflow invariant / needs / no-bot tests — no obsolete “main unprotected” asserts
- [x] 5.2 Aggregator jobs added without changing existing needs edges of report jobs; related tests still green
- [x] 5.3 No obsolete unprotected-main tests to remove

## 6. Ejecutar regresión

- [x] 6.1 `python3 scripts/test_protect_main_ruleset.py` — green
- [x] 6.2 Related workflow unit tests — green
- [x] 6.3 Backend `mvn verify` — n/a for YAML/docs; heavy gate on PR
- [x] 6.4 HTTP/Bruno — n/a for API delta
- [x] 6.5 No `@Disabled` or skipped tests without justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI product surface (ruleset + workflow aggregators + docs)
- [ ] 7.2 Ensure PR still passes repository Playwright job via heavy CI (coordinator)
- [x] 7.3 Serialize if other Playwright-heavy PRs touch `playwright-e2e.yml`
- [x] 7.4 Record "n/a — no UI surface" in PR/traceability

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger — n/a (no endpoint changes)
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for protect-main ruleset
- [x] 8.4 Archive superseded documents — none
- [x] 8.5 Confirm no information was duplicated
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (run before push)

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1040`
- [ ] 9.3 No secrets, no commented-out code, no unrelated product rewrites
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/ci-1040-protect-main-ruleset-69d3`
- [ ] 10.2 Open the PR titled `[#1040] ci(security): protect main with ruleset — required checks, PR-only`, referencing Issue, CU76, CU78
- [ ] 10.3 Wait for every required workflow to pass, including the five named checks
- [ ] 10.4 Gate 4 — merge only on heavy gate (`bash scripts/check-heavy-ci.sh <pr>` exit 0) — coordinator
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main` (coordinator)
- [ ] 11.2 Admin applies ruleset (`apply-protect-main-ruleset.sh --apply`) with **no** durable bot bypass; assert passes
- [ ] 11.3 Record the merge commit, apply evidence, and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: direct push to `main` rejected; ruleset API shows PR + five checks + deletion + non_fast_forward; bypass empty; hooks.md updated
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive ci-1040-protect-main-ruleset`

## Definition of Done

- [ ] Issue linked to Use Cases, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; unit tests written first and observed failing (Gate 2)
- [ ] Implementation passes the applicable suite; heavy CI green; ruleset applied (Gate 3/4/5)
- [ ] Coverage gate unaffected / held (no Java delta)
- [ ] Playwright: n/a product E2E; PR Playwright job still green
- [ ] Permanent documentation updated and consistent (no duplication)
- [ ] Commits atomic, Conventional Commits, `Closes #1040`
- [ ] Pull Request created, CI/CD green, code review approved (Gate 4)
- [ ] Merged to `main` via PR; live ruleset asserted; Issue closed (Gate 5)
