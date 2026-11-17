> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The module was built and benchmarked on this repository (9.6k files) and on spring-framework plus vscode (33k files, about 9M lines). Stores, model caches and benchmark clones are generated and ignored.

## Goals / Non-Goals

**Goals:** a self-contained, tested, benchmarked module that lands without touching product code.
**Non-Goals:** adoption by the Foreman fleet, a hosted service, or support for non-local embedding models.

## Decisions

1. **Module path `framework/ai-sdlc/graph-db`.** It sits with the AI SDLC kit but is its own module with its own `verify.sh`, so the kit's `verify.sh` is unchanged.
2. **SQLite + FTS5, fastembed, scipy.** Free and local; no server to run.
3. **Third-party course material is not committed.** The notebook the design follows stays out of the public repository; the README names the lesson only.

## Riesgos / Trade-offs

- [Generated data committed by mistake] → stores, caches and clones are in `.gitignore`; the PR file list is checked.
- [Heavy first embedding pass on large repositories] → documented; the graph is usable before it ends.

## Testing Strategy

`framework/ai-sdlc/graph-db/tests` (56 tests, coverage floor 88%) covers every scenario; `bench/` reproduces the retrieval, agent, performance and scale measurements.

## Regression Strategy

`bash framework/ai-sdlc/graph-db/verify.sh` and `python3 workspace/tests/test_modules_manifest.py`.

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
