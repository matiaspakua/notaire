# fix: make an exact-symbol search skip the embedding and report adoption honestly

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2325 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/repolens-exact-symbol-47dc` |
| Gate 1 status | passed |

## Objetivo

An exact identifier search spent its time in the embedding query, search alone missed callers, and the usage gate printed `adoption 0%` when there was no harness session to measure.

## What Changes

- Skip `embed_query` when the query is one identifier and the name index already hits.
- Add `calls` predecessors of that hit so a caller is returned even when full-text search is empty.
- Print `adoption n/a` when the log has zero harness sessions.
- Record the Testimony.getDto locate sample in the factory KPI files.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A prose question still uses the embedding | existing search | Made explicit |
| Adoption is a session rate, and CLI calls have no session | `report.py` | Changed |

## Capabilities

### New Capabilities

- None. No HTTP API of the notary product changes.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | RepoLens 0.4.4 inside `framework/ai-sdlc` |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients. A single-identifier search no longer pays for an embedding.

### Write set

- `repolens/engine.py`
- `repolens/report.py`
- `tests/test_graph.py`
- `tests/test_factory.py`
- `CHANGELOG.md`
- `factory-kpis.json`
- `factory-loop-comparison.md`
- `factory-learning-log.md`

### Architecture review

The Pages UI is not edited. The bench files the charts read are updated with measured numbers only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |
| RepoLens changelog | 0.4.4 |

## Out of Scope

- Rebuilding the live index. Search of `Testimony.getDto` still returns no results until the next index, because the node is absent.
- Filing further issues. #2325 is the tracker because #2307 is closed and Gate 1 requires an open issue.
