# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | [#1408](https://github.com/matiaspakua/notaire/issues/1408) | open |
| Use Case | `CU77 – Operations Monitoring and Incident Management` | exists |
| Specification | `docs/openspec/changes/add-grafana-status-dashboards/` | Gate 1 pending |
| Branch | `feat/1408_grafana_status_dashboards` | created |
| Tasks | `tasks.md` | pending plan completion |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| All configured services are reachable | `infra/tests/test_grafana_status_dashboards.py::test_probe_targets_cover_stack_services` | pending |
| A monitored service is unavailable | `infra/tests/test_grafana_status_dashboards.py::test_probe_module_reports_failure` | pending |
| Operator assesses a healthy technology stack | `infra/tests/test_grafana_status_dashboards.py::test_stack_dashboard_has_availability_and_alert_panels` | pending |
| Operator identifies an infrastructure incident | `infra/tests/test_grafana_status_dashboards.py::test_stack_dashboard_has_error_and_alert_queries` | pending |
| Optional telemetry is absent | `infra/tests/test_grafana_status_dashboards.py::test_overview_queries_have_no_data_safe_settings` | pending |
| Operator assesses Notaire readiness | `infra/tests/test_grafana_status_dashboards.py::test_notaire_dashboard_covers_application_services` | pending |
| A Notaire dependency becomes unavailable | `infra/tests/test_grafana_status_dashboards.py::test_notaire_dashboard_uses_probe_statuses` | pending |
| Operator needs detailed diagnosis | `infra/tests/test_grafana_status_dashboards.py::test_overview_dashboards_link_to_specialists` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `infra/MODULE.md` | pending | pending |
| `infra/docs/DEFINITION.md` | pending | pending |
| `infra/docs/OPERATION.md` | pending | pending |
| `docs/200-architecture/207-monitoring/README.md` | pending | pending |
| `docs/200-architecture/202-ADR/ADR-016-observability-stack.md` | pending | pending |
| `CHANGELOG.md` | n/a (not user-visible) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | #1408 open; OpenSpec plan in progress |
| 2 | Failing tests written, test cases designed | pending | Planned infra guard test scenarios above |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None. No Constitution §12 exception has been requested or taken.
