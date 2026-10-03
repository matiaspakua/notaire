# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1185 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists (add #1185 to ID table) |
| Related | #1179 / PR #1184 and #1186 / PR #1188 (their changes are archived here); #1108 (seed script) | referenced |
| Specification | `openspec/changes/ci-1185-clear-pipeline-failures/` | Gate 1 draft |
| Branch | `ci/1185_clear_pipeline_failures` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No active change has a closed issue | `bash scripts/validate-sdlc-plan.sh` exits 0 | pending |
| Archived changes keep their content | `openspec/changes/archive/` listing + diff of moved files | pending |
| Seed script fills values on BSD and GNU sed | `scripts/tests/test_validate_sdlc_plan.py::test_seed_fills_known_header_values` | pending |
| Each release has unique `###` headings | `scripts/test_changelog_structure.py` | pending |
| No changelog entry is lost | same (entry lines identical before and after) | pending |
| Pipeline passes on main without bypass | `bash scripts/run_pipeline.sh` exits 0 | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `openspec/specs/` | pending | — |
| `CHANGELOG.md` | pending | — |
| `CI-PREFLIGHT.md` | pending | — |
| CU76 | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh ci-1185-clear-pipeline-failures` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
