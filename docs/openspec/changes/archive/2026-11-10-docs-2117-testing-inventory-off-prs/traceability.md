# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2117 | open (Gate 1.6 label SKIPPED — ambient Cursor token) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/archive/2026-11-10-docs-2117-testing-inventory-off-prs/` | archived |
| Branch | `docs/2117_testing-inventory-off-prs` | created |
| Tasks | `tasks.md` | groups 1–8 done; 9–12 at PR |
| Commits | `b53183a4` | yes |
| Pull Request | #2226 | open |
| CI run | local `preflight.sh --full` **PASSED** (41) | yes |
| Merge commit | pending | pending |
| Release / tag | n/a (docs/scripts) | pending |
| Smoke test | validate + generate counts | passing |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Inventory regenerates from disk | `InventoryToolingTest.test_generator_reports_real_disk_counts` | passing |
| Validator checks discovery not a committed count table | `InventoryToolingTest.test_validator_passes_when_generated_report_absent_or_stale` | passing |
| Unprefixed Playwright spec rejected | `InventoryToolingTest.test_unprefixed_spec_fails_validation` | passing |
| All specs use TS-nnnn prefix | `SpecNamingTest.test_every_playwright_spec_uses_ts_nnnn_prefix` + validator passes | passing |
| Feature PRs do not share a committed count line | `InventoryToolingTest.test_committed_inventory_has_no_shared_count_table` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/INVENTORY.md` | yes (stub) | `b53183a4` |
| `docs/300-development/303-testing/README.md` | yes | `b53183a4` |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | yes | `b53183a4` |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | yes | `b53183a4` |
| `CHANGELOG.md` | yes | `b53183a4` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this change; validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | `internal/foreman-ops-asli-64/tdd-red.txt` |
| 3 | Suite green, coverage held, docs updated | yes | preflight `--full` PASSED (41) |
| 4 | CI green, review approved, no conflicts | pending | PR #2226 |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner/Foreman |

## Exceptions

Gate 1.6 label write: ambient Cursor GitHub App returned
`Resource not accessible by integration (addLabelsToLabelable)`. Honest SKIP via
`bash workspace/sdlc/add-issue-wip-label.sh 2117`.
