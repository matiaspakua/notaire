# Design — Playwright shards (#1258)

## Context

#1197 P0.3. Prefer after #1257 so docs-only PRs do not pay for three shards.

## Goals / Non-Goals

**Goals:** E2E wall ≤6 min; merged report; flake rate not worse.  
**Non-Goals:** Sharding Bruno; changing product tests’ semantics.

## Decisions

| Decision | Choice | Alternative | Why alt lost |
|----------|--------|-------------|--------------|
| Shard count | 3 | 2 or 4 | Balance runner cost vs wall-clock |
| Failure policy | Merge job fails if any shard failed | Soft-merge | Must not hide red |
| Path filters | Keep #1257 product gate | Always shard | Docs-only must stay cheap |

## Riesgos / Trade-offs

DB/seed contention across shards → isolate fixtures; watch flake rate ~10 runs.

## Testing Strategy

| Scenario | Verification |
|----------|--------------|
| Matrix present | workflow invariants |
| Merge needs all shards | invariants + CI |
| Aggregator fails on shard fail | script review / CI |

## Regression Strategy

Full Playwright on product PRs; compare flake rate post-merge.

## Playwright Strategy

Same suite, sharded execution; no new product coverage required for the CI change itself.
Product PRs still require green Playwright merge gate.

## Deployment Strategy

Workflow-only; next Actions run after merge.

## Rollback Strategy

Revert to single `e2e-tests` job.
