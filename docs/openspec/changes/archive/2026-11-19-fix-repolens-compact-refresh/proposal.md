# fix: smaller RepoLens answers and a non-blocking refresh

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2336 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/repolens-compact-refresh-47dc` |
| Gate 1 status | passed |

## Objetivo

Sonnet input tokens rose when RepoLens answers were fed back into the next turn. The first search after a merge also waited about 5.5s for an incremental index.

## What Changes

- Default search is 4 detailed hits plus 4 one-line hits. Snippet default is 32 lines. Impact default depth is 2. Signatures and symbol lists are shorter.
- A stale query starts the detached index and returns. `post-merge` already starts that same refresh.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A search answer stays short enough to read back as model input | factory KPI, #2336 | Changed |
| A query does not wait for the incremental index | measured 5.52s and 5.79s waits | Changed |

## Capabilities

### New Capabilities

- None.

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
| CI/CD | no | RepoLens 0.4.6 |

### Surface area

- Not **BREAKING** for the HTTP API. CLI and MCP defaults return fewer lines.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| AGENTS.md | the refresh bullet |
| RepoLens changelog | 0.4.6 |
| factory-kpis.json and factory-learning-log.md | the measured byte cut |

## Out of Scope

- A new Sonnet bench run.
- Re-embedding the Notaire index.
