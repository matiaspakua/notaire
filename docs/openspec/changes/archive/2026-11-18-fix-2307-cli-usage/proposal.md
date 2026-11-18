# fix: count CLI queries in the repolens usage report

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2307 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2307-cli-usage-47dc` |
| Gate 1 status | passed |

## Objetivo

`repolens report` stayed at 0 events when the factory used the CLI, because only the MCP server wrote `.repolens/usage.jsonl`. CLI query commands now write the same log, and the report counts them.

## What Changes

- `search`, `node`, `outline`, `snippet`, `neighbors`, `impact`, and `path` append a `src=cli` event.
- `repolens report` includes `src=cli` events in the call totals. The client label is `cli`.
- The MCP extra requires `mcp>=1.2,<2`, because mcp 2.0 removes `FastMCP` and fails `verify.sh`.
- Version 0.4.1.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A CLI query is visible to `repolens report` | #2307 | New |

## Capabilities

### New Capabilities

- None. No product HTTP contract changes.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `framework/ai-sdlc/repolens` | yes | CLI log and report |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Product API: none
- Database: none

### Write set

- `repolens/cli.py`
- `repolens/report.py`
- `repolens/__init__.py`
- `pyproject.toml`
- `CHANGELOG.md`
- `tests/test_interfaces.py`
- factory comparison and learning log

### Architecture review

Factory tooling only. No product ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/repolens/CHANGELOG.md` | 0.4.1 |
| `factory-loop-comparison.md` | paired locate rows |
| Product docs | none |

## Out of Scope

- `impact Deed.getDto` still lists `History.getDto` and `ProcedureEntityTest`, which do not call `Deed.getDto`. That over-link stays filed.
- Migrating the MCP server to mcp 2.
