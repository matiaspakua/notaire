> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). Each signal was verified by hand while building #1462-#1468; this automates it.

## Goals / Non-Goals

**Goals:** one repeatable command, honest PASS/FAIL/SKIP. **Non-Goals:** CI integration, browser dashboard checks.

## Decisions

1. **Python stdlib script** with an injectable `fetch` function so the check logic is unit-tested without a stack. Rejected: a bash script (untestable assertions).
2. **Tempo is queried through Grafana's datasource proxy** because Tempo's port is not published; credentials come from `GRAFANA_ADMIN_*` (defaults match compose).
3. **"Data present" means a non-empty result in the last 30 minutes**, so a stale stack does not pass.
4. **SKIP, never silent pass**, for checks missing credentials.

## Riesgos / Trade-offs

- [Passing means data exists, not that it is correct] -> documented; dashboards were verified separately.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| All signals present | unit | `infra/tests/test_verify_signals.py::test_all_signals_present_exits_zero` |
| A signal is missing | unit | `::test_missing_signal_fails_with_reason` |
| SonarQube is not authenticated | unit | `::test_unauthenticated_sonar_is_skipped` |

Plus a live run on the local stack (recorded in the PR).

- New Java tests: none.

## Regression Strategy

- `bash infra/verify.sh`.

## Playwright Strategy

The script drives the existing Playwright suite (`--with-e2e`); no new spec.

## Deployment Strategy

- Flyway: no. `.env`: none new. Smoke (Gate 5): run it on the local stack.

## Rollback Strategy

- Revert safe: yes.

## Migration Plan

1. Failing unit tests. 2. Script. 3. Live run, including E2E. 4. Docs.
