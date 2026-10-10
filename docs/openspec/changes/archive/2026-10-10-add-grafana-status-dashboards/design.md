> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

See [proposal.md](proposal.md) for motivation. Existing Grafana provisioning
contains focused backend, PostgreSQL, authentication, and log dashboards. The
Prometheus `up` series currently covers only Prometheus, backend, PostgreSQL
exporter, Grafana, and Loki; it cannot report frontend, pgAdmin, Homer,
SonarQube, Promtail, or either PostgreSQL database availability.

The change stays inside the accepted separate Compose topology in ADR-016 and
uses Grafana's existing provisioned Prometheus and Loki data sources.

## Goals / Non-Goals

**Goals:**

- Add observed HTTP/TCP availability signals for the full local Notaire and
  observability stack.
- Provision one technology-stack overview and one concise Notaire-only status
  view, including active alerts and recent error evidence.
- Make the existing focused dashboards the documented drill-down path.

**Non-Goals:**

- Add application instrumentation, alert delivery, a managed monitoring
  product, or a production uptime/SLO service.
- Replace the existing specialist dashboards or change the application UI/API.

## Decisions

1. **Use a pinned Prometheus blackbox exporter with `http_2xx` and
   `tcp_connect` modules.** Prometheus scrapes `/probe` and relabels the
   original target into a stable `service` label. This makes HTTP service and
   database-port availability an explicit `probe_success` signal. Log activity
   was rejected because a live service can emit no logs; cAdvisor/Docker socket
   discovery was rejected because it broadens host privileges and does not
   express application readiness. The image will be pinned to
   `prom/blackbox-exporter:v0.28.0`, matching the repository's pinned-image
   policy.
2. **Probe only non-sensitive readiness endpoints or TCP ports.** Backend uses
   its unauthenticated health endpoint; no probe includes credentials. The
   alternative of probing the protected Actuator metrics endpoint would copy
   credentials into configuration and violates the least-privilege objective.
3. **Provide two overview dashboards, not one dashboard per component.**
   `notaire-stack-status` is the cross-stack incident entry point;
   `notaire-status` presents frontend, backend, PostgreSQL, and pgAdmin in a
   concise operational view. The detailed dashboards retain their present
   responsibility. A single large dashboard was rejected because it mixes
   first-response status with diagnostic telemetry; more component dashboards
   were rejected as duplicate views.
4. **Keep dashboard JSON and its tests in the provisioning tree.** This follows
   the existing file-provisioning contract and makes review/rebuilds
   deterministic. Manual Grafana edits were rejected because they are not
   versioned or testable.

## Riesgos / Trade-offs

- [Probe availability measures a reachable endpoint, not a successful business
  workflow] → Keep backend, database, auth, and logs dashboards as the
  diagnostic layer; do not label an HTTP probe as a complete transaction test.
- [A new exporter increases image-supply-chain surface] → Pin the image,
  include it in existing Compose/image validation, and retain Trivy scanning.
- [Startup transients can show temporary probe failures] → Use Grafana's normal
  no-data/last-value behavior and document that stack health is assessed after
  the existing readiness checks complete.
- [Promtail has no dedicated business endpoint] → Probe its metrics listener;
  log-delivery correctness remains diagnosed through Loki and existing logs.
- [No Alertmanager is deployed] → Show Prometheus active alerts only; routing
  notifications remains outside this change as recorded in ADR-016.

## Testing Strategy

TDD is mandatory: add the infra guard tests below and observe their failure
against the current configuration before adding the exporter, scrape targets,
or dashboards.

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| All configured services are reachable | configuration | `infra/tests/test_grafana_status_dashboards.py::test_probe_targets_cover_stack_services` |
| A monitored service is unavailable | configuration | `infra/tests/test_grafana_status_dashboards.py::test_probe_module_reports_failure` |
| Operator assesses a healthy technology stack | configuration | `infra/tests/test_grafana_status_dashboards.py::test_stack_dashboard_has_availability_and_alert_panels` |
| Operator identifies an infrastructure incident | configuration | `infra/tests/test_grafana_status_dashboards.py::test_stack_dashboard_has_error_and_alert_queries` |
| Optional telemetry is absent | configuration | `infra/tests/test_grafana_status_dashboards.py::test_overview_queries_have_no_data_safe_settings` |
| Operator assesses Notaire readiness | configuration | `infra/tests/test_grafana_status_dashboards.py::test_notaire_dashboard_covers_application_services` |
| A Notaire dependency becomes unavailable | configuration | `infra/tests/test_grafana_status_dashboards.py::test_notaire_dashboard_uses_probe_statuses` |
| Operator needs detailed diagnosis | configuration | `infra/tests/test_grafana_status_dashboards.py::test_overview_dashboards_link_to_specialists` |

- New unit tests (`src/test/java/.../unit/`): None; no Java behavior changes.
- New integration tests (`src/test/java/.../integration/`): None; dashboard
  provisioning is covered by Python configuration guards and live smoke checks.
- Coverage impact (JaCoCo ratchet floor; 80% target): None; no backend bytecode
  changes. The full Maven gate still runs as part of the repository pipeline.

## Regression Strategy

- Existing tests affected: `infra/tests/test_infra_prometheus_hardening.py`,
  existing `infra/tests/test_*.py`, and `bash infra/verify.sh` must continue to
  pass. They protect Compose structure, safe Prometheus operation, and
  observability configuration.
- New configuration guards assert the existing dashboard UIDs and datasource
  UIDs remain intact, so the overview layer cannot replace specialist views.
- Full suite command: `mvn verify -pl backend-api`.
- HTTP/Bruno API suite: `bash testing/scripts/test.sh`.
- Legacy paths at risk: none; `frontend-swing` is retired and no `jpa` or
  frontend application code changes.

## Playwright Strategy

n/a - no Notaire product UI surface changes. Grafana provisioning is validated
as configuration and through the live Grafana/Prometheus smoke checks; the
existing Playwright suite still runs in the full SDLC pipeline for regression.

## Deployment Strategy

- Flyway migration required: no.
- Deployment order / coupling: deploy the application stack first, then the
  observability stack so blackbox probes can resolve the application network;
  Grafana provisioning reloads the dashboard files.
- Configuration or `.env` keys to add: none.
- Feature flag: no.
- Smoke test after deploy (Gate 5): start the full stack, query Prometheus for
  one `probe_success` series per configured service, verify both dashboard UIDs
  through Grafana's API/UI, then verify an unavailable probe produces `0` in a
  controlled local check.

## Rollback Strategy

- Revert safe: yes. Revert the exporter service, its Prometheus scrape and
  probe configuration, the two dashboard files, and documentation together.
- Database rollback: none needed.
- Data written under the new behavior after revert: only short-retention
  Prometheus time series; they expire naturally and require no migration.
- Blast radius if rollback is delayed: the new probes create extra low-volume
  Prometheus scrape traffic but do not affect application request handling.

## Migration Plan

1. Add failing configuration guards for probes, dashboard schema, queries,
   no-data behavior, and drill-down links.
2. Add the pinned blackbox exporter, safe probe modules, and Prometheus
   scrape/relabel configuration.
3. Add the two provisioned dashboard JSON files and update permanent
   observability documentation, including ADR-016's component inventory.
4. Run static infra verification and start the full stack for live probe and
   Grafana provisioning smoke checks.
5. Complete the full SDLC pipeline, then create the PR for #1408.
