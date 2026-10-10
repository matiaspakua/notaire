# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | [#1464](https://github.com/matiaspakua/notaire/issues/1464) | open |
| Use Case | `CU77 – Operations Monitoring and Incident Management` | exists |
| Specification | `docs/openspec/changes/feat-1464-tempo-traces/` | Gate 1 |
| Branch | `feat/1464_tempo_traces` | created |
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
| Collector forwards traces | `infra/tests/test_tempo_traces.py::test_collector_traces_pipeline_to_tempo` | pending |
| A request produces a trace | `infra/tests/test_tempo_traces.py::test_backend_uses_otel_tracer` | pending |
| Datasource is provisioned | `infra/tests/test_tempo_traces.py::test_grafana_tempo_datasource` | pending |

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
| 1 | Issue + Specification + Acceptance Criteria | pending | #1464 open |
| 2 | Failing tests | pending | pending |
| 3 | Suite green, docs updated | pending | pending |
| 4 | CI green, review | pending | pending |
| 5 | Deployed, smoke test, Issue closed | pending | pending |

## Exceptions

None.
