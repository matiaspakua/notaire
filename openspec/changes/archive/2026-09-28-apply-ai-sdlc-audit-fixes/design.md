# Design — apply the AI SDLC audit fixes

## Context

`local-ai/AUDIT.md` lists 26 findings across the policy, specification,
enforcement and harness layers. Today `foreman.sh` parses env files twice (bash
`kv()` and the Python renderer), and neither strips quotes, which stopped #1063.
CI checks PR titles but not commits, TDD order or §12 exceptions.

## Goals / Non-Goals

**Goals:** close the audit findings that are mechanical and small: one parser,
static test gates, a runner for review CHECK lines, JSONL metrics, and four
PR-range checks shared by CI and preflight. Refresh the policy text that
contradicts the code.

**Non-Goals:** the `.aisdlc/project.yml` adapter and splitting `foreman.sh`
(§4), full branch protection (E1), Claude `Stop`/`PostToolUse` hooks (E4),
semantic traceability (S2), post-merge archive (S3). These need their own
design; they become follow-up issues.

## Decisions

- **Checks are small programs, not inline shell.** `bin/envfile.py`,
  `bin/static_checks.py`, `bin/review_check.py` are unit-testable and form the
  first slice of the gates/guards split the audit asks for.
- **One parser.** `kv()` calls `envfile.py`; the renderer imports it. The two
  readers can no longer disagree.
- **PR-range checks take `<base> <head>`** and read only git, so CI and
  preflight run the same script. Labels are passed in by CI (`PR_LABELS`);
  preflight reads them with `gh` when a PR exists, and otherwise skips the label
  part with a note.
- **The exception label is `sdlc-exception`.** The worker cannot set labels
  (no `gh` write in its prompts), so the label records a human decision.
- **Dependabot PRs are exempt** from the exception and TDD checks: they touch no
  code the SDLC governs.
- **SpecKit is archived**, not deleted: `speckit/` moves to `docs/000-archive/speckit/`.

## Riesgos / Trade-offs

- The TDD check is a heuristic (test file changed first), not a real red run in
  CI. A real red run needs the red SHA from the harness ledger; left for later.
- The exception check will fail docs-only PRs until a human adds the label.
  That is the point of S1, but it adds one click per docs PR.
- The rule path lint only checks backticked paths under known top-level
  folders, so it misses prose references.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Double-quoted command; Single-quoted value with trailing comment; Inner quotes are kept | unit | `local-ai/sdlc/tests/test_envfile.py` |
| Absolute home path; Duplicate test class; No new assertion; Clean test change | unit (temp git repo) | `local-ai/sdlc/tests/test_static_checks.py` |
| Literal expectation met; Literal expectation missed; Free-text expectation | unit | `local-ai/sdlc/tests/test_review_check.py` |
| Gate result recorded | unit | `local-ai/sdlc/tests/test_metrics.py` |
| Bad subject; Good subjects | unit (temp git repo) | `scripts/tests/test_pr_checks.py` |
| Production code without tests; Tests first; Code before tests | unit (temp git repo) | `scripts/tests/test_pr_checks.py` |
| No change folder, no label; Labelled exception | unit (temp git repo) | `scripts/tests/test_pr_checks.py` |
| Empty rule file; Dead path | unit (temp dir) | `scripts/tests/test_pr_checks.py` |
| Missing schema line | unit (temp dir) | `scripts/tests/test_pr_checks.py` |

## Regression Strategy

Run both new suites, `bash scripts/validate-sdlc-plan.sh`, and
`bash scripts/preflight.sh --fast` on the branch. No backend or frontend code
changes, so `mvn verify` and the frontend suites must stay unchanged; CI runs them.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Merged through the PR. The new checks take effect on the next PR; the harness
changes take effect on the next `foreman.sh` run (#1063 resumes with them).

## Rollback Strategy

`git revert` of the merge commit. Each check is its own job step, so one noisy
check can also be removed alone.

## Migration Plan

None. Existing merged PRs are not re-checked. Open PRs without a change folder
need the `sdlc-exception` label on their next push.

## Open Questions

None.
