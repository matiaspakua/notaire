> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Measured inputs: the orphan prototype on Notaire (1,116 isolated files, 901 under demoted paths; 853 history-only).

## Goals / Non-Goals

**Goals:** fewer false orphans through better linking, a precise orphan list, no retrieval regression.
**Non-Goals:** new ranking weights; downloading grammars ahead of time.

## Decisions

1. **Tags queries for the long tail.** The 9 hand-written specs stay (Java callers are measured against javac); a language with a tags query uses it; any other grammar uses the generic walker.
2. **Code only where definitions exist.** A detected language without tags query or known walker is text with its language name; HTML, CSS or Terraform gain their mentions, not empty symbol trees.
3. **Proximity before ambiguity.** A path or name resolves to the candidates sharing the longest path prefix with the mentioning file; more than three equally near link nothing.
4. **Orphans from the graph, on demand.** File-level relations are computed from the engine's adjacency arrays (structure excluded, shared virtual nodes count, library imports do not); nothing new is stored.
5. **MCP budget holds.** `orphans` joins the core tools and the instructions are trimmed so the pinned 4,000-byte budget still holds.

## Riesgos / Trade-offs

- [More mention edges shift ranking] → retrieval bench before/after on the tuning and held-out sets.
- [Grammar downloads on a new language] → one attempt per language per process, text fallback.
- [Index time grows] → measured on the 33k-file scale corpus.
- [A false orphan is deleted] → the list is a review aid; the README says how dynamic references hide.

## Testing Strategy

`tests/test_agnostic.py`, `tests/test_orphans.py`, `tests/test_interfaces.py`; `bench/orphan_bench.py`, `bench/perf_bench.py`.

## Regression Strategy

`bash framework/ai-sdlc/repolens/verify.sh`; retrieval bench on the tuning and held-out sets; caller bench.

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
