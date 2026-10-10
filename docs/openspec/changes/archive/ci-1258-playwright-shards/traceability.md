# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1258 | open |
| Parent | #1197 P0.3 | open |
| Related | #1257 (path CI, merged #1422) | linked |
| Use Case | CU76 | linked |
| Specification | `docs/openspec/changes/ci-1258-playwright-shards/` | Gate 1 |
| Branch | `cursor/ci-1258-playwright-shards-cf98` | active |
| Tasks | `tasks.md` | in progress |
| Pull Request | pending | — |

## Requirement coverage

| Scenario (Acceptance Criterion) | Verification | Status |
|---------------------------------|--------------|--------|
| Three-shard matrix | workflow invariants | pass |
| `--shard=` on run step | invariants | pass |
| Merge job fail-closed | invariants + CI | pending CI |
| Heavy-CI check name preserved | invariants | pass |
| Path-scoped skip still OK | aggregator + check-heavy-ci skip | pass |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| OpenSpec / CHANGELOG / CI-PREFLIGHT / CI-MERGE-GATE | yes | this PR |
| REPO-SPLIT-PLAN P0.3 | yes | this PR |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + AC | yes | this change |
| 2 | Invariant tests | yes | PlaywrightShardInvariantsTest |
| 3 | Docs | yes | this PR |
| 4 | CI / PR | pending | |
| 5 | Merged / Issue closed | pending | |

## Exceptions

- Issues write often 403 — sync via PR `Closes #1258` / `Refs #1197`.
- Wall-clock ≤6 min evidenced on first green product PR after merge (note in PR).
- Bruno remains non-sharded in this issue.
