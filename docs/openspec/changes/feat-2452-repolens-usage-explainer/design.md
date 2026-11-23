> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Hook: `repolens/usage.py` `harness_event` handles Read/Grep/Glob and `mcp__repolens__*` only; `setup.py` writes the matcher `Read|Grep|Glob|mcp__repolens__.*`. Report: `report.py` `session_metrics` counts every read after a RepoLens call. Ranking: `engine.py` already has `test_factor` for tests. API: `api.py` serves `/api/metrics` (stats, orphans, usage report, bench files from `REPOLENS_BENCH_RESULTS`) that the UI never calls; `/api/subgraph` returns hits without timings.

## Goals / Non-Goals

**Goals:** Truthful adoption and hit rate; the recorded component miss fixed without lowering benchmark recall; an explorer that answers how RepoLens works and what it changes.
**Non-Goals:** New LLM benchmark sessions; a new frontend stack for the explorer (it stays one HTML file with Cytoscape).

## Decisions

1. Parse the Bash command in the hook (standard library, regex) instead of asking the CLI for a session id: Claude Code does not export its session id to shell commands, and the hook payload already has it. 2. `doc_factor` mirrors `test_factor`: a weight on the boost, a word list to opt out, one tuning key. 3. Per-hit explanation reuses `why` (`match` = anchor, else the relation); timings come from `engine.timings`, added to the `/api/subgraph` response. 4. Benchmark files are classified by their `summary` arms (`baseline`/`graph` = agent, `grep`/`graph` = retrieval); nothing adopter-specific is named in the package.

## Riesgos / Trade-offs

Bash parsing can misclassify an unusual command (for example `grep` inside a heredoc): the event is advisory telemetry, never a gate. `doc_factor` could hide a doc a code-worded question wanted: the doc anchor still seeds the walk, and the held-out set guards it.

## Testing Strategy

Unit tests in `framework/repolens/tests` (pytest): Bash payloads (repolens search, rg, git grep, unrelated command), outside-repository read, report on a legacy log, setup matcher, `doc_factor` on a fixture graph, `/api/subgraph` timings, `/api/metrics` bench classification, `serve --bench` parsing. Retrieval benchmark before/after on both task sets.

## Regression Strategy

`bash framework/repolens/verify.sh` (lint, full suite with the coverage floor, wheel, smoke). Retrieval benchmark recall@10 and MRR not lower on tuning and held-out tasks.

## Playwright Strategy

The explorer is RepoLens's own single HTML page, served by `repolens serve`, outside the Notaire frontend and its Playwright suite. Verified in a browser at 320, 768 and 1280 px in light and dark with the keyboard; screenshots in the PR.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
