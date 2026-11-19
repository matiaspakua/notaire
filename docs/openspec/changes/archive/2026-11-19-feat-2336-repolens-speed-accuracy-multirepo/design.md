> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Measured inputs: bench/results/factory-loop-comparison.md, factory-kpis.json, factory-learning-log.md (cloud factory loop, 2026-11-18/19).

## Goals / Non-Goals

**Goals:** every number before/after on real data: callers vs javac, CLI latency, embed volume, schema bytes.
**Non-Goals:** changing the ranking weights; a hosted index.

## Decisions

1. **Ground truth from javac.** The Compiler Tree API resolves every call; repolens is scored against it instead of a hand-picked sample.
2. **Strict on unresolvable chains.** No edge beats a guessed edge (measured: F1 0.971 vs 0.959).
3. **Unix-socket service per worktree.** Thin stdlib client, idle exit, version check, `REPOLENS_DAEMON=0` escape hatch.
4. **Federation by rank fusion over registered stores**, not one giant store: each repository keeps its own index and lifecycle.

## Riesgos / Trade-offs

- [A resident process outlives the agent] → idle exit and `repolens daemon stop`.
- [Stale service code after a pull] → the client compares versions and restarts it.
- [Strict linking drops a real caller] → measured recall stays above 0.4.5's.

## Testing Strategy

`tests/test_types.py`, `tests/test_daemon.py`, `tests/test_cache.py`, `tests/test_repos.py`, `tests/test_interfaces.py` (schema budget); `bench/caller_bench.py`, `bench/perf_bench.py`.

## Regression Strategy

`bash framework/ai-sdlc/repolens/verify.sh`; retrieval bench on the tuning and held-out sets.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
