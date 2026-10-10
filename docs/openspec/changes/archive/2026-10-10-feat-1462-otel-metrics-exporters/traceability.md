# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | [#1462](https://github.com/matiaspakua/notaire/issues/1462) | open |
| Use Case | `CU77 – Operations Monitoring and Incident Management` | exists |
| Specification | `docs/openspec/changes/feat-1462-otel-metrics-exporters/` | Gate 1 |
| Branch | `feat/1462_otel_metrics_exporters` | created |
| Tasks | `tasks.md` | planned |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| Collector is scraped | `infra/tests/test_otel_metrics_pipeline.py::test_collector_service_and_scrape` | pending |
| Backend metrics arrive through the collector | `infra/tests/test_otel_metrics_pipeline.py::test_backend_exports_otlp_and_keeps_actuator` | pending |
| Frontend is scraped | `frontend/src/tests/unit/metrics-route.test.ts` | pending |
| Container and host targets are up | `infra/tests/test_otel_metrics_pipeline.py::test_exporters_scraped` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `ADR-016-observability-stack.md` | pending | pending |
| `infra/docs/DEFINITION.md` | pending | pending |
| `docs/200-architecture/207-monitoring/README.md` | pending | pending |
| `CHANGELOG.md` | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | #1462 open |
| 2 | Failing tests | pending | pending |
| 3 | Suite green, docs updated | pending | pending |
| 4 | CI green, review | pending | pending |
| 5 | Deployed, smoke test, Issue closed | pending | pending |

## Exceptions

None.
