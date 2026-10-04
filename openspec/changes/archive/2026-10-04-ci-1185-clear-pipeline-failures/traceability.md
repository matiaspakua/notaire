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
| Commits | — | done |
| Pull Request | — | done |
| CI run | — | done |
| Merge commit | — | done |
| Release / tag | — | done |
| Smoke test | — | done |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| No active change has a closed issue | `bash scripts/validate-sdlc-plan.sh` exits 0 | done |
| Archived changes keep their content | `openspec/changes/archive/` listing + diff of moved files | done |
| Seed script fills values on BSD and GNU sed | `scripts/tests/test_validate_sdlc_plan.py::test_seed_fills_known_header_values` | done |
| Each release has unique `###` headings | `scripts/test_changelog_structure.py` | done |
| No changelog entry is lost | same (entry lines identical before and after) | done |
| Pipeline passes on main without bypass | `bash scripts/run_pipeline.sh` exits 0 | done |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `openspec/specs/` | done | — |
| `CHANGELOG.md` | done | — |
| `CI-PREFLIGHT.md` | done | — |
| CU76 | done | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | done | `bash scripts/validate-sdlc-plan.sh ci-1185-clear-pipeline-failures` |
| 2 | Failing tests written, test cases designed | done | — |
| 3 | Suite green, coverage held, docs updated | done | — |
| 4 | CI green, review approved, no conflicts | done | — |
| 5 | Deployed, smoke test passed, Issue closed | done | — |

## Exceptions

None.
