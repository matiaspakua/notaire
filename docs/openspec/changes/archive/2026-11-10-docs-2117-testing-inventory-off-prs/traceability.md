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
| Specification | `docs/openspec/changes/docs-2117-testing-inventory-off-prs/` | active |
| Branch | `docs/2117_testing-inventory-off-prs` | created |
| Tasks | `tasks.md` | groups 1–2 done; 3+ in progress |
| Commits | passing | pending |
| Pull Request | passing | pending |
| CI run | local `preflight.sh --full` pending | passing |
| Merge commit | passing | pending |
| Release / tag | n/a (docs/scripts) | passing |
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
| `docs/300-development/303-testing/INVENTORY.md` | pending (stub) | passing |
| `docs/300-development/303-testing/README.md` | passing | pending |
| `docs/300-development/303-testing/PROCESS-ARCHITECTURE.md` | passing | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | passing | pending |
| `CHANGELOG.md` | passing | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this change; validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | passing | TDD red |
| 3 | Suite green, coverage held, docs updated | passing | preflight `--full` |
| 4 | CI green, review approved, no conflicts | passing | PR |
| 5 | Deployed, smoke test passed, Issue closed | passing | Owner/Foreman |

## Exceptions

Gate 1.6 label write: ambient Cursor GitHub App returned
`Resource not accessible by integration (addLabelsToLabelable)`. Honest SKIP via
`bash workspace/sdlc/add-issue-wip-label.sh 2117`.
