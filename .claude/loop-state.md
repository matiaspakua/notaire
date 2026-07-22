# Loop State

This file is the memory for the autonomous issue-loop agent (`.claude/agents/issue-loop.md`).
It did not exist before the 2026-07-22 01:00 Europe/Madrid scheduled run — created now.

## Current

- current_issue: null
- status: pending
- branch: null
- pr_url: null
- blocked_reason: null

## Completed (this and prior runs, from git history)

- 648, 649, 650, 652 (merged before this loop-state.md file existed; see `git log --oneline main`)
- 560 — security(auth): login rate limiting and account lockout. PR #653, squash-merged at
  2026-07-22T18:46:59Z. Found and fixed a real pre-existing bug along the way (see PR body):
  UsuarioController.login() fell through to its "not found" branch even after a match, which
  would have double-counted failed attempts against the new lockout counter.
- 561 — security(validation): Jakarta Bean Validation on UsuarioController + ReporteController
  (the issue's own suggested starting point). PR #654, squash-merged at 2026-07-22T19:18:51Z.
  Filed #655 to track the full rollout across the remaining ~29 controllers as deliberate
  follow-up rather than claiming it done here.
- 587 — test(e2e): E2E coverage reports were a static hardcoded template. PR #656, squash-merged
  at 2026-07-22T19:33:03Z. Its own CI couldn't exercise the changed `coverage-report` job
  (path-filtered to frontend/**/backend-api/**) — see #658 below for what that first real run
  found.
- 610 — test(e2e): mobile/tablet viewport coverage in Playwright. PR #657, squash-merged at
  2026-07-22T19:57:07Z. Its CI run (the first to touch frontend/** since #587 merged) exposed
  a real crash in #587's script — filed and fixed as #658, not folded into this PR since the
  bug belongs to #587's file, not #610's.
- 658 (unplanned, same-run hotfix) — generate_e2e_coverage_report.py crashed on the real Bruno
  CLI JSON shape (array of iterations, not a dict with "summary"). PR #659, squash-merged at
  2026-07-22T20:11:41Z. Verified the fix against the actual failing job's downloaded artifact
  rather than guessing the schema again; also fixed the reason Playwright results always
  showed "not found" (playwright-e2e.yml passed `--reporter=html,json,junit` on the CLI,
  overriding playwright.config.ts's own reporter array and its correct outputFile paths).

## Notable discovery this run — NOT yet acted on, flagged for a future run

While downloading real CI artifacts to fix #658, found that **83 of 86 Bruno API contract
tests (137 of 147 sub-assertions) are currently failing on `main`** — e.g. `GET
/api/v1/registro-auditoria` returns 401 Unauthorized in the Bruno run. This looks like recent
JWT/auth-enforcement changes (issue #552 and friends — requests without a Bearer token now
correctly rejected with 401) broke a large fraction of the Bruno collection, which likely still
sends unauthenticated requests. This is a real, currently-broken state on `main`, unrelated to
any of this run's 4 selected issues — worth a dedicated future issue/run rather than being
folded into an unrelated PR. Not filed as a GitHub issue yet; flagging here first since it may
warrant a look before triage (could be "Bruno collection needs auth token wiring" or could be
a genuine backend regression — needs a human or a dedicated investigation to tell which).

## This run's summary (2026-07-22 01:00 Europe/Madrid, executed ~17:50–20:12 UTC)

5 PRs merged (4 planned + 1 unplanned same-run hotfix): #653, #654, #656, #657, #659.
1 follow-up issue filed for future work: #655 (bean validation rollout).
1 discovery flagged above for future triage (Bruno API test failures on main).
No blockers. All CI green (except the two known-flaky jobs, ignored per policy) on every
merged PR.

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
