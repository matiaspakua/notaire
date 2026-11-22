# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2394 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2394-factory-store-telemetry/` | complete |
| Branch | `feat/2394_factory_store_telemetry` | created |
| Tasks | `tasks.md` | 9/12 complete (groups 10–12 pending PR/merge/deploy) |
| Commits | see below | recorded |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

### Commits

| SHA | Message |
|-----|---------|
| `67a78f5e` | `[#2394] docs(openspec): factory store and mandatory telemetry change` |
| `c55e7dae` | `[#2394] docs(openspec): correct store change artifacts` |
| `6a3fa829` | `[#2394] test(aisdlc): failing tests for the factory store and telemetry` |
| `18023409` | `[#2394] test(aisdlc): tighten failing store and telemetry tests` |
| `43ebd63a` | `[#2394] feat(aisdlc): add factory store, telemetry sync, CLI and serve route` |
| `ef6d42c6` | `[#2394] fix(aisdlc): redesign dual-write to evidence._append mirror` |
| `12c09a99` | `[#2394] fix(aisdlc): redesign dual-write to evidence._append mirror` |
| `bbfb4bb0` | `[#2394] fix(aisdlc): fix serve error code and add errors.py to write set` |
| `5a215654` | `[#2394] fix(aisdlc): correct stage-run import, intent propagation, dead code, mirror error handling` |
| `76160b18` | `docs(aisdlc): OpenAPI, README and changelog for the factory store and telemetry` |
| `7e3dc40b` | `[#2394] docs(aisdlc): restore changelog layout and fix telemetry schema type` |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| first migration creates all tables | `test_store.py::test_schema_creates_all_tables` | passing |
| re-running migration is a no-op | `test_store.py::test_migration_idempotent` | passing |
| store file is git-ignored | `test_store.py::test_gitignore_contains_factory_db` | passing |
| sync imports ledger rows | `test_telemetry_store.py::test_sync_ledger` | passing |
| sync is idempotent — second run adds no rows | `test_telemetry_store.py::test_sync_idempotent` | passing |
| sync imports cost log entries | `test_telemetry_store.py::test_sync_cost` | passing |
| sync reads RepoLens usage defensively | `test_telemetry_store.py::test_sync_repolens` | passing |
| RepoLens line with missing keys is handled | `test_telemetry_store.py::test_sync_repolens_malformed_and_missing_keys` | passing |
| record a telemetry row | `test_telemetry_store.py::test_record_redacts_secrets` | passing |
| report groups by stage with correct sums | `test_telemetry_store.py::test_report_group_stage` | passing |
| report groups by agent with failure counts | `test_telemetry_store.py::test_report_group_agent` | passing |
| store stats returns row counts | `test_telemetry_store.py::test_stats` | passing |
| record with a secret-shaped key is redacted | `test_telemetry_store.py::test_record_redacts_secrets` | passing |
| no secret value exists in any telemetry row after sync | `test_telemetry_store.py::test_no_secrets_in_rows` | passing |
| report with no store file | `test_telemetry_store.py::test_absent_store_empty_report` | passing |
| stats with no store file | `test_telemetry_store.py::test_absent_store_empty_stats` | passing |
| telemetry endpoint returns report with read token | `test_telemetry_store.py::test_telemetry_endpoint_with_read_token` | passing |
| telemetry endpoint rejects unauthenticated request | `test_telemetry_store.py::test_telemetry_endpoint_no_token_401` | passing |
| telemetry endpoint rejects invalid group | `test_telemetry_store.py::test_telemetry_endpoint_invalid_group_400` | passing |
| report includes failure counts by integration | `test_telemetry_store.py::test_report_failures_by_integration` | passing |
| report includes repolens summary | `test_telemetry_store.py::test_report_repolens_summary` | passing |
| GET /factory/telemetry matches CLI report | `test_telemetry_store.py::test_telemetry_endpoint_with_read_token` | passing |
| RepoLens input and files never stored | `test_telemetry_store.py::test_repolens_no_input_or_files` | passing |
| report groups by intent | `test_telemetry_store.py::test_report_group_intent` | passing |
| report math (sums, counts) | `test_telemetry_store.py::test_report_math` | passing |
| dual-write fail-open | `test_telemetry_store.py::test_mirror_fail_open_runs_start`, `test_mirror_fail_open_harness` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | yes | `76160b18` |
| `framework/ai-sdlc/CHANGELOG.md` | yes | `76160b18`, `7e3dc40b` |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | yes | `76160b18` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `67a78f5e` — OpenSpec change created and validated |
| 2 | Failing tests written, test cases designed | yes | `6a3fa829`, `18023409` — failing tests committed before implementation |
| 3 | Suite green, coverage held, docs updated | yes | Suite 535 tests passed; README, CHANGELOG, OpenAPI updated |
| 4 | CI green, review approved, no conflicts | pending | Awaiting PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Awaiting merge |

## Exceptions

Deviations recorded in `tasks.md` (group 4):
- Dual-write via `evidence._append` mirror instead of `runs.py`/`harness.py` hooks.
- `sync_keys` table added for idempotency tracking.
- RepoLens rows stored as telemetry rows (`agent='repolens'`), not a separate table.
- `errors.py` added to write set for invalid group error code.
