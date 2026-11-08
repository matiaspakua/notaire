# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1739 | open → PR closes |
| Use Case | CU77 – Operations Monitoring and Incident Management | exists |
| Specification | `docs/openspec/changes/archive/2026-10-15-feat-1739-frontend-otel-spans/` | archived |
| Branch | `cursor/feat-1739-frontend-otel-2ea7` | created |
| Tasks | `tasks.md` | implementation complete; CI pending |
| Commits | `558ac823`, `3d04d671`, `21674d83` | done |
| Pull Request | [#1751](https://github.com/matiaspakua/notaire/pull/1751) | open (draft) |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| OTLP proxy forwards traces to collector URL | `frontend/src/tests/unit/otlp-proxy.test.ts` | verified |
| Tracing init is opt-in and names the service | `frontend/src/tests/unit/otel-browser-tracing.test.ts` | verified |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/207-monitoring/README.md` | yes | pending |
| `frontend/README.md` / `.env.example` / `.env.local.example` | yes | pending |
| `CHANGELOG.md` | yes — Unreleased Features | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1739, archived change |
| 2 | Failing tests written, test cases designed | yes | vitest otlp-proxy + otel-browser-tracing |
| 3 | Suite green, coverage held, docs updated | yes | vitest 4/4; CHANGELOG Features after #1718 |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
