# fix: refresh a stale RepoLens index before answering

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2331 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/repolens-index-freshness-47dc` |
| Gate 1 status | passed |

## Objetivo

A query answered from an index whose commit was not HEAD. Cloud VMs merge without the git hooks, so the next query has to notice and refresh.

## What Changes

- Record `indexed_head` at the end of an index run.
- Before `search`, `impact`, and the other read commands, refresh when that commit is not HEAD, and say so on stderr.
- Keep the git hooks. They are no longer the only trigger.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An answer uses the graph of the current HEAD when the refresh can run | measured miss on Testimony.getDto | Changed |
| A busy index still answers, and says it is answering from the previous graph | existing index lock | Made explicit |

## Capabilities

### New Capabilities

- None. The notary HTTP API does not change.

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
| CI/CD | no | RepoLens 0.4.5 |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients. A stale query pays for an incremental index before it prints results.

### Write set

- `repolens/freshness.py`
- `repolens/indexer.py`
- `repolens/cli.py`
- `repolens/service.py`
- `tests/test_factory.py`
- changelog, README, AGENTS.md, factory bench notes

### Architecture review

The Pages UI is not edited. The bench files those charts read record the fixture measurement only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |
| AGENTS.md locate note | one bullet |
| RepoLens changelog | 0.4.5 |

## Out of Scope

- Timing an incremental refresh of the full Notaire index.
- Replacing the git hooks.
