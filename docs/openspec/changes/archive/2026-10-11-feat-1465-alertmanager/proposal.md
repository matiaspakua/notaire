# Alertmanager and alert rules for the full stack

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1465 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1465_alertmanager` |
| Gate 1 status | Pending |

## Objetivo

Prometheus evaluates five alert rules but `alerting.alertmanagers` is an empty list, so no alert is routed, grouped
or visible outside Prometheus. Deploy Alertmanager, wire Prometheus to it, and cover the signals that matter for
Notaire with tested rules.

## What Changes

- Add Alertmanager (pinned) with a local-only route (no external notification channel) and group/inhibit rules.
- Wire `prometheus.yml` to it and provision an Alertmanager datasource in Grafana.
- Add rules for 5xx ratio, API latency, scrape target down, database connections, Hikari pool saturation, host memory
  and disk, and tracing/log pipeline health; keep the five existing rules.
- Run `promtool check rules` and `amtool check-config` in a guard test, plus structural guards.
- Document the alert catalogue; update ADR-016 (Alertmanager is no longer "not deployed").

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must be notified of availability and error incidents. | CU77 | Made explicit |
| Every alert must carry severity, a summary and a description. | CU77 | New |

## Capabilities

### New Capabilities
- `alert-routing`: Alertmanager receives, groups and exposes Notaire alerts.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | Existing Actuator/Hikari/HTTP metrics are the sources. |
| `frontend` | No | |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Alertmanager service/config, rules, datasource, tests, docs. |
| CI/CD | No | Existing gates. |

### Surface area

- Endpoints / entities / Flyway / `.env`: none. Image `prom/alertmanager:v0.34.1`, port 9093.
- Compatibility: additive. Real notification channels (email/Slack) are out of scope; the receiver is a local no-op
  webhook-less `default` receiver so alerts are visible in the Alertmanager UI and Grafana.

### Architecture review

Reverses the ADR-016 decision "Alertmanager not deployed"; ADR-016 is updated in this change (no new ADR needed because
the decision record itself is amended).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `ADR-016-observability-stack.md` | Alertmanager deployed; inventory and "not deployed" statement. |
| `docs/200-architecture/207-monitoring/README.md` | Alert catalogue and routing. |
| `infra/docs/DEFINITION.md`, `infra/docs/OPERATION.md` | Service and how to read/silence alerts. |
| `CHANGELOG.md` | N/A: operational. |

## Out of Scope

- External notification channels; per-container alerts (cAdvisor reports no container names on Docker Desktop);
  dashboards (#1467).
