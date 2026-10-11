> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). Rules exist but nothing receives them.

## Goals / Non-Goals

**Goals:** deployed, wired, validated Alertmanager and a meaningful rule catalogue. **Non-Goals:** external channels, per-container alerts, dashboards.

## Decisions

1. **Alertmanager with a local `default` receiver.** Alerts are visible in its UI and Grafana; no secrets or external services needed.
   Rejected: email/Slack (needs credentials; owner decision).
2. **Validate with the real tools** (`promtool` from the pinned Prometheus image, `amtool` from the Alertmanager image) via `docker run`
   in the guard test, skipping with a clear message when Docker is unavailable. Rejected: YAML-shape checks only (miss PromQL errors).
3. **Rules use signals that carry names** (probe, JVM, HTTP, Hikari, Postgres, node-exporter). Per-container rules rejected: cAdvisor on Docker
   Desktop reports cgroup ids only.
4. **Inhibit warnings when a critical alert of the same `service` fires** to avoid alert storms.
5. **New rules in a new file `alert-rules-stack.yml`** so the existing file stays untouched and reviewable; both are loaded.

## Riesgos / Trade-offs

- [Noisy alerts on a laptop] -> conservative thresholds and `for:` durations.
- [Missing series make rules silent] -> every rule's metric was confirmed present on the live stack.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Alertmanager is wired | configuration | `infra/tests/test_alertmanager.py::test_prometheus_targets_alertmanager` |
| Rules are valid and complete | configuration + promtool | `::test_every_alert_has_severity_summary_description`, `::test_promtool_accepts_rules` |
| Routing is valid | configuration + amtool | `::test_routing_groups_and_inhibits`, `::test_amtool_accepts_config` |
| Datasource is provisioned | configuration | `::test_grafana_alertmanager_datasource` |

Plus a live check that a stopped service fires `ServiceDown` and appears in Alertmanager.

- New Java tests: none. Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh`; existing `test_grafana_status_dashboards.py` (alert rules mount) stays green.

## Playwright Strategy

n/a - no UI change.

## Deployment Strategy

- Flyway: no. `.env`: none. Restart Prometheus (rules/config bind-mounted) and Grafana.
- Smoke (Gate 5): stop a service, see its alert firing in Alertmanager, restart it, see it resolve.

## Rollback Strategy

- Revert safe: yes (service, config, rules file, datasource).

## Migration Plan

1. Failing guards. 2. Alertmanager + config. 3. Rules. 4. Datasource. 5. Docs. 6. Live fire test.
