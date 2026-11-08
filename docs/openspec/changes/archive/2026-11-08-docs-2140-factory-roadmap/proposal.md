# Durable multi-session AI SDLC factory roadmap

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2140 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure (RNF-90) |
| Branch | `cursor/docs-factory-roadmap-69d3` |
| Gate 1 status | passed |

## Why

Later sessions rediscover the factory goal from store assessments. The in-repo foundation slice docs do not yet carry a durable roadmap for the full profile-driven factory.

## What Changes

Add FACTORY-ROADMAP.md covering the full factory goal as sequenced milestones; point FACTORY-FOUNDATION and the framework index at it; keep a workspace test that the roadmap covers the required topics. No TelemetryPort code (#2125 stays parallel).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The durable factory plan lives in-repo under docs/300-development/306-ai-sdlc-framework/; TelemetryPort code is out of scope for #2140 | CU76 / #2140 / CONSTITUTION P12 | New |

## Capabilities

### New Capabilities

- `factory-roadmap`: In-repo multi-session AI SDLC factory roadmap that later agents continue from

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | docs under 306-ai-sdlc-framework plus workspace/tests/test_factory_roadmap.py |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `docs/300-development/306-ai-sdlc-framework/**`
- `docs/300-development/README.md`
- `workspace/tests/test_factory_roadmap.py`
- `docs/openspec/changes/docs-2140-factory-roadmap/**`

### Architecture review

Follows the existing architecture; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | new durable roadmap |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-FOUNDATION.md` | pointer to roadmap |
| `docs/300-development/306-ai-sdlc-framework/ai_sdlc_framework.md` | link roadmap |
| `docs/300-development/README.md` | index entry |
| `CHANGELOG.md` | omitted this PR — serialize Unreleased with #2125 |

## Out of Scope

TelemetryPort SDK (#2125). OpenFeature/OpenAPI/HTTP UI implementation. Isolator runtime. Moving workspace/sdlc or product code into the kit.
