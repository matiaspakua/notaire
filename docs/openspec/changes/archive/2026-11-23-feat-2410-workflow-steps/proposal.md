# Edit workflow steps with their line stage

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2410 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2410_workflow_steps` |
| Gate 1 status | passed |

## Objetivo

The production-line diagram (#2407) places a run on its station through the stage of the running workflow step. The approved `factory-feature` workflow is a demo seed whose only step has stage `implement`, and the kit cannot write a v2 workflow's steps, so runs never land on a station.

## What Changes

- `workflows.new_version` accepts `steps` (v2): validated (unique `id`, required `stage`, `on_fail` stop|continue, typed optional keys) and written as a new draft revision; `expected_version` gives optimistic concurrency.
- CLI `aisdlc workflow edit --id <id> --steps-file <file> [--summary]`.
- API `PUT /factory/workflows/{id}` (write scope, CSRF, `If-Match`), contract `factory-control` 1.6.0 (additive).
- `factory-feature` gets a new approved version whose steps map onto the line: spec→plan, red→red, green→pre-push, docs and archive→pre-pr, merge→merge, release→release, signal→signal.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every workflow step names its line stage | #2410; #2396 production-line contract | New |
| Approved workflows stay immutable; editing returns a workflow to draft | #2254 governance FSM | Made explicit |

## Capabilities

### New Capabilities

- `aisdlc-workflows`: editing a workflow's v2 steps with their line stage.

### Modified Capabilities

None.

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
| `framework/ai-sdlc` | yes | workflows.new_version steps, CLI edit, PUT route, OpenAPI 1.6.0 |
| `.aisdlc` workflow catalog | yes | factory-feature version mapped onto the line |

### Surface area

<!-- Entities, endpoints, database schema (Flyway migration needed?), configuration,
     external dependencies. Name the endpoints and the migration version explicitly.
     State whether any change is BREAKING for API clients. -->

- Entities: none.
- Endpoints: `PUT /factory/workflows/{id}` (new, additive).
- Database (Flyway `V{n}`): none.
- Configuration / `.env`: a new `factory-feature` revision in `.aisdlc/catalogs/workflows/`.
- Dependencies: none.

### Write set

- `framework/ai-sdlc/aisdlc/workflows.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/serve.py`
- `framework/ai-sdlc/aisdlc/runs.py`
- `framework/ai-sdlc/aisdlc/status.py`
- `framework/ai-sdlc/tests/test_workflow_steps_edit.py`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/CHANGELOG.md`
- `.aisdlc/catalogs/workflows/factory-feature/**`
- `docs/openspec/changes/**`
- `docs/openspec/changes/archive/**`
- `docs/openspec/specs/aisdlc-workflows/**`

### Architecture review

No ADR: the governance FSM and pinning stay as they are; this only lets a draft carry v2 steps.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | `workflow edit` command and the PUT route |
| `framework/ai-sdlc/CHANGELOG.md` | `[Unreleased]` entry |

## Out of Scope

- Editing steps from the dashboard UI.
