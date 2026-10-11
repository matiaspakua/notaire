> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). Homer's `connectivity_test` runs in the browser, so it only works for published ports.

## Goals / Non-Goals

**Goals:** complete Homer, guarded against drift. **Non-Goals:** server-side checks, restyling.

## Decisions

1. **Services without a published port link to the Grafana health dashboard** (`notaire-observability-health`), where their probes and scrape status already show, and carry `connectivity_test` off implicitly by pointing at Grafana.
2. **Guard maps compose service names to Homer entry names** through a small explicit alias table in the test, so a rename shows up as a failure instead of silently passing.
3. **Dashboards are checked by UID substring in link URLs**, derived from the provisioned JSON files.

## Riesgos / Trade-offs

- [Alias table must be maintained] -> the failure message names the missing service.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| No service is missing | configuration | `infra/tests/test_homer_hub.py::test_every_compose_service_is_listed` |
| No dashboard is missing | configuration | `infra/tests/test_homer_hub.py::test_every_dashboard_and_sonar_project_is_linked` |

- New Java tests: none.

## Regression Strategy

- `bash infra/verify.sh`.

## Playwright Strategy

n/a - no product UI change.

## Deployment Strategy

- Flyway: no. `.env`: none. Restart the Homer container to reload the config.
- Smoke (Gate 5): Homer serves and its config lists all groups.

## Rollback Strategy

- Revert safe: yes.

## Migration Plan

1. Failing guard. 2. Extend the config. 3. Verify live.
