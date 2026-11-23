# run start --intent

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2426 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/2426_run_start_intent` |
| Gate 1 status | passed |

## Objetivo

A run started from the CLI never carries its intent (found in the 2026-11-23 factory test), so status and the dashboard say no intent is attached.

## What Changes

- `aisdlc run start` gains `--intent <id>`: validated, recorded on the run, and the run is attached to the intent, like `POST /factory/runs`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A run started from any entry point carries its intent | #2395 one entry point for intents | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `factory-intents`: the CLI run start carries the intent.

## Impact Analysis

### Módulos afectados

<!-- Mark every module this change touches. Delete rows that do not apply, but do
     not delete the table: an empty table means the analysis was not done. -->

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `framework/ai-sdlc` | yes | `run start --intent` |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: none.
- Endpoints: none.
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: none.
- Dependencies: none.

### Write set

- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/tests/test_run_start_intent.py`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `docs/openspec/changes/**`
- `docs/openspec/specs/factory-intents/**`

### Architecture review

No ADR: the CLI calls the same functions the API does.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | `run start --intent` |
| `framework/ai-sdlc/CHANGELOG.md` | `[Unreleased]` entry |

## Out of Scope

None.
