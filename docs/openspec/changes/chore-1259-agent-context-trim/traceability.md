# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1259 | open |
| Parent | #1197 P0.4 | open |
| Related | #1257 (path CI #1422), #1419 (metrics baseline, merged) | linked |
| Use Case | CU76 | linked |
| Specification | `docs/openspec/changes/chore-1259-agent-context-trim/` | Gate 1 |
| Branch | `cursor/chore-1259-agent-context-cf98` | active |
| Tasks | `tasks.md` | in progress |
| Pull Request | pending | — |

## Requirement coverage

| Scenario (Acceptance Criterion) | Verification | Status |
|---------------------------------|--------------|--------|
| Budget ≤8k after trim | `agent-context-budget.py --max-tokens 8000` + unit test | pass (~2034) |
| Constitution card ≤3072 bytes | unit test + file size | pass |
| frontend-design not alwaysApply | unit test / frontmatter | pass |
| check-agent-rules green | `bash workspace/sdlc/check-agent-rules.sh` | pass |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `AGENTS.md` / `CONSTITUTION-AGENT-CARD.md` | yes | this PR |
| `REPO-METRICS-BASELINE.md` | yes | this PR |
| `docs/300-development/304-ai-sdlc-cloud/` | yes | this PR |
| `CHANGELOG.md` | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + AC | yes | this change |
| 2 | Budget unit tests | yes | `test_agent_context_budget.py` |
| 3 | Docs / baseline | yes | this PR |
| 4 | CI / PR | pending | |
| 5 | Merged / Issue closed | pending | |

## Exceptions

- Issues write often 403 — sync via PR body `Closes #1259` / `Refs #1197`.
- Product Playwright / Bruno: waived (no product UI). Process + unit guards only.
- Full `CONSTITUTION.md` stays authoritative; card is digest only.
