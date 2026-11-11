# Phase A — factory entity versioning + workflow governance

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2239 |
| Use Case | CU76 |
| Branch | `feat/2239_factory-phase-a-domain` |
| Gate 1 status | passed |

## Objetivo

Owner backoffice/runtime UIs need a single honest domain contract before any SPA:
immutable content versions for factory entities and a real workflow governance
lifecycle (`draft → defined → validated → approved`). Catalogs today overwrite
digests (#2139) and have no workflow kind or OpenAPI write surface.

## What Changes

- Immutable revision history + current pointer for catalog-backed factory entities
- Extended catalog kinds: `workflows`, `providers`, `integrations`, `rules`
  (plus existing `skills|mcp|agents|memory`)
- First-class workflow entity with fail-closed governance FSM; `approve` wired
  to ApprovalPort honesty (or documented Owner CLI override)
- OpenAPI 3.1 schemas/paths for entities, versions, workflows, transitions
- CLI create/list/show/new-version/history/transition (+ approved filter);
  JSON ≡ OpenAPI; `aisdlc contract check` green
- Kit-only tip under `framework/ai-sdlc` (+ OpenSpec/docs); no SPA / Playwright product UI

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Entity updates allocate a new immutable revision; prior revision bytes must not be rewritten | CU76 / #2239 | New |
| Workflow governance states are exactly `draft\|defined\|validated\|approved` | CU76 / Owner plan | New |
| Illegal transitions fail closed; list/show never execute workflow steps | CU76 / #2239 | New |
| `approve` requires ApprovalPort verify when live, else Owner override path or fail closed | ADR-005 / #2205 | Made explicit for workflows |
| Runtime may only select workflows in `approved` state | CU76 / Phase D prep | New |
| Secrets/credentials stored as refs (`env:…`) only; redaction on show | factory redaction / #2139 | Made explicit for providers/integrations |
| Stages ≠ workflows | #2182 vs #2239 | Made explicit |

## Capabilities

### New Capabilities

- `factory-entity-governance`: Versioned factory entities, extended kinds, workflow
  governance FSM, OpenAPI/CLI write surface for Phase A (no SPA).

### Modified Capabilities

- `catalogs`: Extend kinds and replace overwrite-update with immutable revisions +
  current pointer; list/show/history never execute.
- `factory-contract`: Extend OpenAPI 3.1 with entity/version/workflow schemas and
  write/list paths consistent with CLI JSON.
- `approval`: Workflow `approve` transition consumes ApprovalPort verify honesty.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | domain, CLI, OpenAPI, tests, kit README |

### Surface area

- Entities: FactoryEntity, EntityVersion, Workflow (+ governance state/transitions);
  thin providers/integrations/rules kinds
- Endpoints: kit OpenAPI paths only (CLI SoR; no HTTP server required)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: optional Owner override env for approve when not live;
  credential refs as `env:…` strings in catalog JSON
- Dependencies: none new (stdlib + existing ApprovalPort / ssh-keygen)

### Write set

- `framework/ai-sdlc/aisdlc/**`
- `framework/ai-sdlc/openapi/**`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/MODULE.md`
- `docs/openspec/changes/feat-2239-factory-entity-governance/**`
- `docs/openspec/specs/factory-entity-governance/**` (on archive)
- `docs/openspec/specs/catalogs/**` (delta on archive)
- `docs/openspec/specs/factory-contract/**` (delta on archive)
- `docs/openspec/specs/approval/**` (delta on archive if needed)

### Architecture review

Follows kit Ports & trust (ADR-001/002), OpenAPI 3.1 (ADR-003), ApprovalPort
(ADR-005). No new ADR; no Isolator/sandbox claim; no product OpenAPI import.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document versioned catalog/workflow CLI + kinds |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Entity/workflow schemas + paths |
| `CHANGELOG.md` | omit Unreleased (fence / release-please; note in Issue if needed) |
| Store plan `docs/ai-sdlc-factory-uis-plan.md` | coherence only (store); not a repo permanent doc |

## Out of Scope

SPA / backoffice chrome (#2240–#2242); Notaire product Playwright; Context Lake /
Port.io clone; Wilson vanity stages; fake Isolator; `[committer] required=true`;
HTTP server mandatory; executing MCP/agents on list; epic #2238 close.
