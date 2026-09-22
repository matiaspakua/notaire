# Traceability — preflight-ci-drift-audit

## Chain

Issue #1029 → this OpenSpec change (`preflight-ci-drift-audit`, `skip_specs: true`,
no spec deltas — pure tooling-doc fix) → branch
`chore/1029_preflight_ci_drift_audit` → commit(s) on that branch → PR against
`main` referencing #1029 → CI (`pr-validation.yml` — SDLC Plan Validation,
Code Lint) → merge → issue closed.

No Use Case is affected (CONSTITUTION.md documented-exception precedent, same
as #973): this changes a diagnostic string printed by a local script, not any
runtime or API behavior.

## Requirement coverage

| Requirement | Covered by |
|-------------|------------|
| `scripts/preflight.sh --list` accurately states CI blocking/advisory status for every row | Manual review of `.github/workflows/ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml` against the table (issue #1029 body, drift table row 1–4); fixed row 1, verified rows 2–4 already correct |
| JaCoCo ratchet floor claim (70% line / 25% branch) matches enforced reality | Read `backend-api/pom.xml` `jacoco:check` rule directly — confirmed match, no code change required |

## Permanent documentation updated

- `scripts/preflight.sh` (`--list` heredoc) — the only file this change edits.
- No `docs/` file required an update: the audit found the JaCoCo floor
  documentation (CONSTITUTION.md, `.claude/rules/code-quality.md`) already
  accurate, so nothing there needed correction.

## Gate log

- Gate 1 (Issue + Use Case + Specification): passed — Issue #1029 open,
  documented Use Case exception, proposal/design/tasks complete,
  `skip_specs: true`.
- Gate 2 (TDD): n/a — this change edits a heredoc string in a bash script,
  not executable logic with a meaningful failing-test state; verified by
  running `bash scripts/preflight.sh --list` before/after and diffing output.
- Gate 3 (full suite + docs): `bash scripts/validate-sdlc-plan.sh` run
  against this change; `bash scripts/preflight.sh` run before push (see
  PR for any environment limitations encountered, e.g. Docker/server-backed
  suites not exercised in this sandbox — noted honestly rather than skipped
  silently).
- Gate 5 (smoke test / close): PR merged, issue #1029 closed via
  `Closes #1029` in the commit.

## Exceptions

- No Use Case: documented technical/process exception, precedent #973
  (epic with no business Use Case, technical refactor tracked directly
  against the issue).
- `skip_specs: true`: no spec-level behavior changes — this is a developer
  tooling accuracy fix, not a capability change.
