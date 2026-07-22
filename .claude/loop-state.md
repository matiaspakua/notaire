# Loop State

This file is the memory for the autonomous issue-loop agent (`.claude/agents/issue-loop.md`).
It did not exist before the 2026-07-22 01:00 Europe/Madrid scheduled run — created now.

## Current

- current_issue: 561
- status: in_progress
- branch: fix/561_bean-validation
- pr_url: null
- blocked_reason: null

## Completed (this and prior runs, from git history)

- 648, 649, 650, 652 (merged before this loop-state.md file existed; see `git log --oneline main`)
- 560 — security(auth): login rate limiting and account lockout. PR #653, squash-merged at
  2026-07-22T18:46:59Z. Found and fixed a real pre-existing bug along the way (see PR body):
  UsuarioController.login() fell through to its "not found" branch even after a match, which
  would have double-counted failed attempts against the new lockout counter.

## This run's queue (2026-07-22 01:00 Europe/Madrid)

Selected top candidates from the 2026-07-02 repository audit issues (labels `priority:*`),
after excluding RF-XX/RNF-XX/CU-* tracking issues, TAREA-labeled issues, and pure
`docs(...)`-only "create a guide" issues (not bug/test/refactor/a11y/security in nature):

1. #560 — security(auth): login endpoint has no rate limiting or account lockout (priority:high)
2. #561 — security(validation): zero Jakarta Bean Validation usage (priority:high) — scoped to
   the issue's own suggested starting point (UsuarioController + ReporteController), full rollout
   across all 31 controllers tracked as a follow-up issue.
3. #587 — test(e2e): E2E coverage reports are a static hardcoded template (priority:high)
4. #610 — test(e2e): no mobile/responsive viewport coverage in Playwright config (priority:high)

### Deferred this run (judgment call — not skipped permanently, see Policy)

- #566 (ci: no quality/security gate can ever fail the build, priority:critical) — investigated
  first since it's critical priority. Found the codebase currently has **3069 Checkstyle
  warnings** and **234 SpotBugs findings**. Flipping `failOnViolation`/`failOnError` to true as
  the issue suggests would immediately red-CI every future PR (including the other 3 issues in
  this run) until those pre-existing findings are fixed — that is a large, multi-week cleanup
  project, not a well-scoped single-issue fix. Needs a dedicated effort (fix violations first,
  then flip the gate, ratchet-style like the existing JaCoCo floor) rather than being rushed in
  an autonomous cycle. Left open for a deliberate follow-up.
- #559 (security(authz): no RBAC enforcement anywhere in the backend, priority:high) — requires
  per-endpoint judgment calls about which of 31 controllers/endpoints need which role, which is
  a domain-knowledge-heavy design task, not mechanical. Deferred for a focused pass rather than
  guessed at speed.
- #567 (security(deps): outdated JasperReports 3.5.3, priority:high) — a major version bump of
  the report-generation engine risks breaking compiled `.jasper` templates; needs careful
  regression testing of report generation, deferred rather than rushed.

## Policy (skip rules, carried forward for future runs)

- Skip RF-XX / RNF-XX requirement-tracking issues and CU-* use-case tracking issues (they are
  documentation/traceability placeholders, not actionable work items).
- Skip TAREA-labeled issues and large architectural `arch(...)` issues.
- Skip pure `docs(...)`-only "create a new guide" issues — not bug/test/refactor/a11y/security.
- Treat issues that would require touching 25+ files / a systemic multi-week cleanup as
  "large architectural" in spirit even if not literally labeled that way — defer them with a
  written reason (see "Deferred this run" above) rather than doing a misleading partial fix that
  closes the issue without resolving the underlying problem.
