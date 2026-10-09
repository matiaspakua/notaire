# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1315 | open → in progress |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas | exists |
| Specification | `docs/openspec/changes/fix-1315-accepted-breaking-changes-dir/` | Gate 1 draft |
| Branch | `fix/1315_accepted_breaking_changes_dir` | stacked on #1382 |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | #1389 | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Empty directory | `test_check_accepted_breaking_changes.py` `test_empty_directory_passes_and_ignores_nothing` | passing |
| Live entry of the pull request | `test_live_entry_of_the_pull_request_is_ignored`, `test_files_of_several_pull_requests_are_concatenated`, `RealOasdiffTest` | passing |
| Stale or malformed entry | `test_stale_entry_fails_and_names_file_and_entry`, `test_file_name_must_start_with_the_issue_number`, `test_entry_needs_an_issue_comment_above_it`, `test_entry_must_have_the_oasdiff_shape` | passing |
| Legacy list | `test_legacy_single_list_fails_with_a_migration_hint`, `test_directory_exists_and_the_legacy_list_is_gone` | passing |
| Merged file | `MergedFilesTest` (4 tests) | passing |
| Prune | `test_prune_deletes_merged_files_only` | passing |
| Wiring | `WiringTest`, `test_dast_contract_backup_assets.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `docs/200-architecture/208-devsecops/README.md` | yes | branch commit |
| `docs/300-development/303-testing/TEST-PLAN.md` | yes | branch commit |
| `backend-api/openapi/accepted-breaking-changes.d/README.md` | new | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1315-accepted-breaking-changes-dir` |
| 2 | Failing tests written, test cases designed | yes | 19/21 and 3/12 failing at the test commit |
| 3 | Suite green, coverage held, docs updated | partial | workspace, contracts, docs, security verify green; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box.
