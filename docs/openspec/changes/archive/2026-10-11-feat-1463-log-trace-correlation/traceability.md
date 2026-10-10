# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | [#1463](https://github.com/matiaspakua/notaire/issues/1463) | open |
| Use Case | `CU77 – Operations Monitoring and Incident Management` | exists |
| Specification | `docs/openspec/changes/feat-1463-log-trace-correlation/` | Gate 1 |
| Branch | `feat/1463_log_pipeline_trace_correlation` | created |
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
| API request is logged with its trace id | `RequestLoggingFilterTest::shouldLogRequestWithTraceIdFromMdc` | pending |
| Sensitive and noisy data is excluded | `RequestLoggingFilterTest::shouldNotLogQueryString`, `::shouldSkipActuatorPaths` | pending |
| Proxied request is logged | `frontend/src/tests/unit/backend-proxy.test.ts` | pending |
| Upstream failure is logged as an error | `frontend/src/tests/unit/backend-proxy.test.ts` | pending |
| Derived field is provisioned | `infra/tests/test_log_trace_correlation.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/207-monitoring/README.md` | pending | pending |
| `infra/docs/DEFINITION.md` | pending | pending |
| `CHANGELOG.md` | n/a | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | #1463 open |
| 2 | Failing tests | pending | pending |
| 3 | Suite green, docs updated | pending | pending |
| 4 | CI green, review | pending | pending |
| 5 | Deployed, smoke test, Issue closed | pending | pending |

## Exceptions

None.
