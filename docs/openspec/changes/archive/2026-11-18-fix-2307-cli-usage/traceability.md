# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2307 | open |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2307-cli-usage/` | drafted |
| Branch | `cursor/2307-cli-usage-47dc` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Search and impact are counted | `test_cli_search_is_counted_by_the_usage_report` | passing after the fix |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/repolens/CHANGELOG.md` | 0.4.1 | this change |
| `factory-loop-comparison.md` | paired locate rows | this change |
| `factory-learning-log.md` | this run | this change |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification | yes | validate after fill |
| 2 | Failing test | yes | missing usage.jsonl |
| 3 | Suite green | no | pending verify.sh and preflight |
| 5 | Owner merge | no | #2307 stays open for the impact over-link |

## Exceptions

No exception. The impact over-link is out of scope for this patch and stays on #2307.
