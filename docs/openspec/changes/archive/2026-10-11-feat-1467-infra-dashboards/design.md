> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). #1408 added overviews; #1462-#1465 added the telemetry these views need.

## Goals / Non-Goals

**Goals:** four drill-down dashboards, the missing scrape targets, a live query validation. **Non-Goals:** per-container views, Homer, new PostgreSQL dashboard.

## Decisions

1. **One dashboard per concern** (host, frontend, observability health, traces), not one large board; each links to the others.
2. **Host metrics from node-exporter, containers only in aggregate.** cAdvisor on Docker Desktop has no container names, so per-container panels would show opaque ids.
3. **Frontend request panels read the structured `api_proxy` logs** from Loki (#1463), because the frontend exposes no request histogram; a request histogram is a possible follow-up.
4. **Traces via a Tempo TraceQL table** plus Loki `http_request` lines; trace-id links use the derived field from #1463.
5. **Scrape Tempo/Alertmanager/Promtail natively** and enable the collector's internal metrics, so the health dashboard shows real data.

## Riesgos / Trade-offs

- [Panels referencing absent metrics] -> a live validation runs every query against the datasources before merge.
- [Docker Desktop metrics describe the VM] -> titles say "Docker host".

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Tool targets exist | configuration | `infra/tests/test_infra_dashboards.py::test_tool_scrape_jobs_and_collector_telemetry` |
| Dashboards are provisioned and safe | configuration | `::test_dashboards_are_provisioned_and_linked`, `::test_value_panels_have_no_data_settings`, `::test_queries_use_provisioned_datasources` |
| Queries return data on the live stack | live | scripted query run via the Grafana proxy (recorded in the PR) |
| Existing UIDs intact | configuration | `::test_existing_dashboards_preserved` |

- New Java tests: none. Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh`; `test_grafana_status_dashboards.py` stays green.

## Playwright Strategy

n/a - no product UI change; E2E validation is #1469.

## Deployment Strategy

- Flyway: no. `.env`: none. Reload Prometheus, restart the collector (bind-mounted config); Grafana reloads dashboards on its own.
- Smoke (Gate 5): every panel query returns without error on the live stack.

## Rollback Strategy

- Revert safe: yes (dashboard files, scrape jobs, collector telemetry setting).

## Migration Plan

1. Failing guards. 2. Scrape jobs and collector telemetry. 3. Dashboards. 4. Live query run. 5. Docs.
