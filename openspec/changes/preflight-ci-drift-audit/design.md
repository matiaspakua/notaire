# Design — preflight-ci-drift-audit

## Context

`scripts/preflight.sh` exists precisely because `mvn verify` alone does not
predict CI (Spotless drift, #705). Its `--list` flag prints a table mapping
every local check to the CI job it mirrors, and states whether that CI job
is blocking or advisory. That table is only useful if it stays accurate.
This change re-audits it against the current `.github/workflows/*.yml`
content and the current `backend-api/pom.xml` JaCoCo configuration.

## Goals / Non-Goals

**Goals**
- Verify every row of `scripts/preflight.sh --list` against the actual
  current CI workflow files.
- Verify the JaCoCo ratchet floor claim (70% line / 25% branch) in
  CONSTITUTION.md against `backend-api/pom.xml`.
- Fix any drift found that is safe and mechanical.
- Document, rather than silently resolve, any drift that requires a
  judgment call (see the ESLint blocking-vs-advisory decision in
  `proposal.md` "Out of Scope").

**Non-Goals**
- Fixing #701 (the underlying `eslint-config-next` crash) — out of scope,
  tracked separately.
- Adding new CI gates or removing existing ones — this is an audit of the
  existing map, not a redesign of the gate set.
- Touching `.github/workflows/*.yml` — the audit found no workflow-side
  drift requiring a workflow change; only the local script's printed
  documentation needed correction.

## Decisions

- **Fix the `--list` annotation, not the workflow or the local check's
  blocking behavior.** The drift found (row 1 in the issue's table) has the
  CI step *more* permissive (`continue-on-error: true`) than the local
  check (`run`, hard-blocking). That direction cannot itself produce a
  "local green / CI red" surprise — the local gate is already the stricter
  one. Loosening the local check to `run_warn` to "match" CI would only
  reopen the door to shipping ESLint regressions unnoticed locally. So the
  fix is purely to make the printed map honest about that asymmetry, not to
  change either check's actual behavior.
- **No spec deltas (`skip_specs: true`).** Nothing here changes an
  externally observable requirement — it corrects a diagnostic string.

## Riesgos / Trade-offs

- Risk: a future CI workflow edit (e.g. resolving #701 and flipping ESLint
  back to blocking) will re-introduce drift in this same spot if
  `scripts/preflight.sh` isn't updated alongside it. Mitigated by the
  existing repo convention (CLAUDE.md: "When you add or change a gate in
  `.github/workflows/`, update `scripts/preflight.sh` in the same PR") —
  unchanged by this proposal, just reaffirmed.
- Trade-off: this change is deliberately narrow (one annotation) rather than
  a broader "make everything symmetric" pass, because every other row
  audited was already accurate — inventing extra changes would just be
  churn against a table that mostly works.

## Testing Strategy

No unit/integration/E2E surface exists for a bash heredoc string. Verified
manually:
- `bash scripts/preflight.sh --list` output reviewed before and after the
  edit to confirm only the intended row changed and the table still renders
  correctly (fixed-width alignment).
- `bash scripts/validate-sdlc-plan.sh preflight-ci-drift-audit` run to
  confirm this OpenSpec change itself passes Gate 1.

## Regression Strategy

`bash scripts/preflight.sh` (or at minimum a syntax check, `bash -n
scripts/preflight.sh`) run before push to confirm the script still executes
without error after the edit. No other script or workflow file is touched,
so no broader regression surface exists.

## Playwright Strategy

N/A — no UI change.

## Deployment Strategy

None — this is a repo-local developer tooling change with no runtime
deployment; it ships the moment the PR merges to `main`.

## Rollback Strategy

Revert the single commit touching `scripts/preflight.sh`'s `--list` heredoc;
no data, schema, or running-service state is affected.
