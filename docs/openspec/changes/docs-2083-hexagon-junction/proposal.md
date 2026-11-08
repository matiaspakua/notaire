# Junction instrument for hexagonal architecture

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2083 |
| Use Case | RNF-05 – Aspecto visual |
| Branch | `feat/2083_hexagon-anatomy-figure` |
| Gate 1 status | passed |

## Objetivo

A reader of the hexagonal architecture page should see ports and adapters as a machine, not as a ring of labelled plugs. The sealed case is the domain, glands on its skin are the ports, and detachable plug modules are the adapters. The AI Factory home should point at the gated line already drawn on the lifecycle page, with one sentence, instead of mounting that figure a second time.

## What Changes

Replace the hexagonal plug manifold on `/docs/hexagonal/` with a junction instrument: a sealed brass case on a plinth, inbound glands on the front face, outbound glands on the rear of the lid, and three plug modules (HTTP web, JPA persistence, PDF) that mate only to glands. The live readout names the gland and the dependency rule. On `/ai-sdlc/`, replace the embedded gated line with a short carriage-and-boom sentence that links to `/ai-sdlc/lifecycle/`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Dependencies point inward: an adapter depends on a port, a port on the domain | ADR-030 | Made explicit as the figure's readout |
| Public pages explain the architecture without inventing counts | github-pages-technical-docs | Made explicit: the caption uses `hexagon()` counts |

## Capabilities

### New Capabilities

### Modified Capabilities

- `github-pages-technical-docs`: The hexagonal page leads with the junction instrument, and the factory home points at the lifecycle figure.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | no | — |
| `github-page` | yes | junction figure, hexagonal page, factory home, Vitest |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- `github-page/**`
- `docs/openspec/**`
- `CHANGELOG.md`

### Architecture review

Follows the existing architecture; no ADR. The figure explains ADR-030; it does not change it.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased line for the junction figure |
| Public hexagonal and AI Factory pages | The figure and the one-sentence pointer are the documentation |

## Out of Scope

A second isometric figure on the factory home. Backend or frontend product code. Reopening the ArchUnit rules.
