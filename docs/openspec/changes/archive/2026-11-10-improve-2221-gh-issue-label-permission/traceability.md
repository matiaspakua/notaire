# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2221 | open (label ACL SKIPPED — ambient Cursor token) |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/archive/2026-11-10-improve-2221-gh-issue-label-permission/` | archived |
| Branch | `docs/2221_gh-issue-label-permission` | created |
| Tasks | `tasks.md` | groups 1–8 done; 9–12 at PR |
| Commits | see git log on branch | pending SHAs at commit |
| Pull Request | pending | pending |
| CI run | local `preflight.sh --full` **PASSED** (41) | yes |
| Merge commit | pending | pending |
| Release / tag | n/a (docs/scripts) | pending |
| Smoke test | `add-issue-wip-label.sh 2221` → SKIPPED (403) | passing |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| docs name issues write and addLabelsToLabelable | `AddIssueWipLabelDocsTest.test_docs_name_issues_write_and_addLabelsToLabelable` | passing |
| docs say where Owner grants label write | `AddIssueWipLabelDocsTest.test_docs_say_where_owner_grants_cloud_or_fleet_token` | passing |
| addLabelsToLabelable 403 is honest SKIP | `AddIssueWipLabelScriptTest.test_permission_denied_is_honest_skip_not_success` (+ live #2221) | passing |
| successful label reports labeled | `AddIssueWipLabelScriptTest.test_success_reports_labeled` | passing |
| agent workflow points at the helper | `AddIssueWipLabelDocsTest.test_workflow_points_at_helper_not_raw_gh_only` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | yes | pending |
| `docs/300-development/304-ai-sdlc-cloud/FLEET-ARCHITECTURE.md` | yes | pending |
| `.claude/skills/ai-agent-workflow/SKILL.md` | yes | pending |
| `.claude/rules/ai-agent-workflow.md` | yes | pending |
| `docs/openspec/schemas/notaire-sdlc/templates/tasks.md` | yes | pending |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | this change; validate-sdlc-plan |
| 2 | Failing tests written, test cases designed | yes | `internal/foreman-ops-asli-63/tdd-red.txt` |
| 3 | Suite green, coverage held, docs updated | pending | preflight `--full` |
| 4 | CI green, review approved, no conflicts | pending | PR |
| 5 | Deployed, smoke test passed, Issue closed | pending | Owner/Foreman |

## Exceptions

Gate 1.6 label write: ambient Cursor GitHub App returned
`Resource not accessible by integration (addLabelsToLabelable)`. Honest SKIP via
`add-issue-wip-label.sh`; not a Constitution §12 process exception — documented
capability gap (#2221). Do not reopen #2217 or #2218.
