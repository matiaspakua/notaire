# Restore weekly k6 load-test script

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1047 |
| Use Case | **CU74** – Performance and Caching Strategy; **CU76** – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1047-k6-load-test-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1048 merges** (queue: #1057 → #1048 → #1047) |

## Objetivo

`.github/workflows/performance-test.yml` still runs
`performance-test/k6/load-test.js`, but that script was deleted in commit
`b822a18` (“Clean up”, 2026-08-11). Every scheduled/manual run since has failed
at the k6 step (9/9 observed failures). `scripts/test_performance_test_assets.py`
also fails because the file is missing. The project therefore has no working
automated load testing despite #594 (which added it) being closed. Restore (or
rewrite for the English login DTO) a k6 script with SLO-tied thresholds so the
weekly workflow is green and publishes results.

## What Changes

- Recreate `performance-test/k6/load-test.js` for the current English API
  (`POST /api/v1/usuarios/login` with `{ name, password }`, Bearer JWT) covering
  highest-traffic reads: `/api/v1/gestiones`, `/api/v1/presupuestos`,
  `/api/v1/tramites`.
- Declare k6 `stages` and `thresholds` aligned with CU74 SLOs (p95 latency and
  error-rate budgets).
- Emit a durable summary artifact (e.g. `handleSummary` → `summary.json`) so the
  workflow’s `upload-artifact` step publishes results (not silently ignore).
- Keep / lightly adjust `.github/workflows/performance-test.yml` only as needed
  for path, env, and artifact wiring (schedule + `workflow_dispatch` remain;
  not a per-PR gate).
- Update `scripts/test_performance_test_assets.py` assertions for the restored
  script (English login fields, thresholds, summary output).
- Document the restored load-test path under CU74/CU76 and CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Automated load tests MUST exist for top-traffic authenticated read endpoints | CU74 load baseline; #594 / #1047 AC | Made explicit (asset was deleted) |
| k6 thresholds MUST reflect CU74 latency/error SLOs (p95 objective + failure rate) | CU74 AC (p95 &lt; 2s objective; response &lt; 10s); #1047 AC | Made explicit |
| Weekly Performance workflow MUST run green on schedule and manual dispatch | CU76 QA infra; #1047 AC | Made explicit |
| Load-test results MUST be published as a CI artifact | #1047 AC | New / reinforced (upload currently ignores missing files) |

## Capabilities

### New Capabilities

- `k6-load-test-restore`: Restored k6 suite + CI wiring with SLO thresholds and
  published summary artifact for the weekly Performance workflow.

### Modified Capabilities

- (none under `openspec/specs/` today cover the k6 performance suite)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Runtime unchanged; workflow already builds/starts API for k6 |
| `frontend` | no | — |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | yes (minimal) | `performance-test.yml` path/artifact wiring if needed |
| Scripts / perf assets | yes | `performance-test/k6/load-test.js` restored; asset unittest updated |

### Surface area

- Entities: none
- Endpoints: exercised only (login + GET gestiones/presupuestos/tramites); no API contract change
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none for product; workflow already supplies CI JWT/admin/actuator env
- Dependencies: k6 via `grafana/k6-action@v0.3.1` (already wired)

### Architecture review

No product architecture change. Restores the CU74/CU76 load-test capability
deleted by `b822a18`. Script must use English DTO fields (`name`/`password`),
not the Spanish fields from the original #594 asset. Workflow still starts a
local Postgres + backend for the job; prefer minimal restore over redesigning
the stack (Flyway-vs-`ddl-auto=update` alignment is a follow-up risk, not
required to satisfy #1047 AC). Serialize implement after **#1048** per fleet
queue (after #1057).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU74 – Performance and Caching Strategy.md` | Note restored automated k6 load suite + SLO thresholds / weekly CI |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note Performance workflow asset + artifact publication |
| Related perf docs if they still claim missing assets (#303 context) | Point to restored `performance-test/k6/load-test.js` |
| `CHANGELOG.md` | `[Unreleased]` test/CI entry: restore k6 load-test (#1047) |

## Out of Scope

- Making the load test a required per-PR gate.
- Full JMeter/Gatling suite or multi-region soak tests.
- Changing product SLOs or caching implementation (CU74 optimization work).
- Reworking Performance workflow to Flyway-only schema bootstrap (optional
  hardening; not required for AC if backend already becomes healthy).
- Closing #303 (docs-only open issue) unless a one-line cross-link is natural.
- Starting implement / PR before **#1048** merges.
