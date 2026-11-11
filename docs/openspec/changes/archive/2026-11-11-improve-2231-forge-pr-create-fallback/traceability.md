# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2231 | open (label ACL SKIPPED — ambient Cursor token) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/archive/2026-11-11-improve-2231-forge-pr-create-fallback/` | archived |
| Branch | `docs/2231_forge-pr-create-fallback` (+ `cursor/2231-forge-pr-create-fallback-36ad`) | created |
| Tasks | `tasks.md` | groups 1–8 + archive done; 9–12 at PR |
| Commits | pending SHAs after commit | pending |
| Pull Request | pending | pending |
| CI run | pending `preflight.sh --full` | pending |
| Merge commit | pending | pending |
| Release / tag | n/a (docs) | pending |
| Smoke test | docs unittest green | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| docs name createPullRequest friction | `ForgePrCreateFallbackDocsTest.test_docs_name_createPullRequest_friction` | passing |
| docs prefer ManagePullRequest on cursor tip | `ForgePrCreateFallbackDocsTest.test_docs_prefer_manage_pull_request_on_cursor_tip` | passing |
| docs give Owner compare-link fallback | `ForgePrCreateFallbackDocsTest.test_docs_give_owner_compare_link_fallback` | passing |
| docs keep FLEET_APP optional without claiming PR scopes granted | `ForgePrCreateFallbackDocsTest.test_docs_keep_fleet_app_optional_no_false_pr_grant` | passing |
| steward or workflow points at cloud docs | `ForgePrCreateFallbackDocsTest.test_workflow_points_at_cloud_docs` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-LESSONS-HEX-1693.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | yes | pending |
| `.claude/skills/steward/SKILL.md` | yes | pending |
| `.claude/skills/ai-agent-workflow/SKILL.md` | yes | pending |
| `.claude/agents/cloud-foreman.md` | yes | pending |
| `.claude/rules/ai-agent-workflow.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | validate-sdlc-plan; Gate 1.6 SKIP |
| 2 | Failing tests written, test cases designed | yes | `internal/foreman-ops-asli-68/tdd-red.txt` |
| 3 | Suite green, coverage held, docs updated | pending | preflight `--full` |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner/Foreman |

## Exceptions

Gate 1.6 label write: ambient Cursor GitHub App returned
`Resource not accessible by integration (addLabelsToLabelable)`. Honest SKIP via
`add-issue-wip-label.sh`; not a Constitution §12 process exception — documented
capability gap (#2221). Do not reopen #2218.
