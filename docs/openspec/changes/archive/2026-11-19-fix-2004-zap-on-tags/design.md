> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`codeql.yml` already runs on `v*` and `notaire-v*`, on Monday, and on manual dispatch. `dast-zap.yml` had the Monday cron and `workflow_dispatch` only. A tag could ship with CodeQL and without ZAP.

## Goals / Non-Goals

**Goals:**

- The same `zap-baseline` job runs when a `v*` or `notaire-v*` tag is pushed.
- Monday `0 5 * * 1` and manual dispatch remain.
- `fail_action` stays true and the rules file stays `.zap/rules.tsv`.

**Non-Goals:**

- A pull-request ZAP run.
- An authenticated scan.
- New ignore rules.

## Decisions

Copy the tag list from `codeql.yml`: `push.tags` is `v*` and `notaire-v*`, with no `branches` and no `paths`. A `paths` filter would skip the tag run. The job body is unchanged, including `cmd_options: -a` (baseline ajax spider, not an exploit script).

## Riesgos / Trade-offs

- [A tag push waits on a 30-minute ZAP job] → the job already exists on Monday; the tag is the release the scan is meant to cover. It is not a pull-request check (#2001).
- [Maven Central can 429 the `mvn package` step inside the job] → that is the same external flake as dependency submission. Re-run the failed job later. Do not add a retry loop in this change.

## Testing Strategy

| Scenario | Test level | Test |
|----------|------------|------|
| Tags `v*` and `notaire-v*` are present, branches and paths are absent | unit | `test_dast_zap_runs_on_version_tags_and_keeps_monday` |
| Monday cron, `workflow_dispatch`, and no `pull_request` | unit | same test |
| `fail_action` true, rules file, baseline `-a` | unit | same test |

TDD: the new test failed with `AssertionError: 'v*' not found in []` before the trigger was added.

## Regression Strategy

- `python3 -m unittest workspace.tests.test_ci_workflow_invariants security.tests.test_zap_dast_ratchet security.tests.test_security_gate_surface security.tests.test_assessment_currency`
- `bash workspace/sdlc/preflight.sh --full` before push.

## Playwright Strategy

n/a — no product UI.

- Specs under `testing/e2e/tests/`: none

## Deployment Strategy

- Flyway: no
- The new trigger takes effect on the next version tag after merge. Monday's cron is unchanged.

## Rollback Strategy

- Revert safe: yes. Removing the `push` block restores Monday-and-manual only.
- Database rollback: none
