# Phase B — factory backoffice shell (workflows / agents / skills)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2240 |
| Use Case | CU76 — factory control |
| Branch | `feat/2240_factory-backoffice-shell` |
| Gate 1 status | passed |

## Objetivo

Operators need a thin kit-neutral backoffice over Phase A catalogs and workflow
governance — list/detail/create/edit for workflows, agents, and skills — without
a Port.io portal or Notaire product chrome. Phase A (#2239/#2244) shipped the
domain contract; Phase B adapts it into a right-nav HTML UI.

## What Changes

- Kit-local backoffice shell under `framework/ai-sdlc` (extend `#2142 report --html`
  direction): right module nav; brand **AI SDLC factory** (not Notaire product)
- HTML list/detail/create/edit for **workflows**, **agents**, **skills** via
  adapters over Phase A CLI/OpenAPI/ports only — **no second config truth**
- Workflow screen surfaces governance state + allowed transitions; cannot jump to
  `approved` without Phase A ApprovalPort-honest guards
- Version history visible; edits allocate new revisions via Phase A
- Credentials never shown as plaintext (reuse Phase A redaction)
- Optional thin stdlib `backoffice serve` for interactive POST mutations; static
  `--html` export for read-only snapshots
- Kit HTML/contract tests only — no Notaire Playwright product suite edits

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Backoffice UI is an adapter over Phase A catalogs/workflows only | CU76 / #2240 | New |
| Right module nav; one job per module; kit-neutral brand | Owner plan / #2240 | New |
| Workflow UI may offer only Phase A–legal transitions for current state | CU76 / #2239 | Made explicit in UI |
| Approve path remains ApprovalPort-honest (or Owner override) — UI cannot bypass | ADR-005 / #2205 | Made explicit in UI |
| Edits create new immutable versions via Phase A write paths | CU76 / #2239 | Made explicit in UI |
| Credentials/refs never rendered as plaintext | factory redaction / #2139 | Made explicit in UI |
| No Notaire product branding inside kit UI paths | #2240 / kit independence | New |

## Capabilities

### New Capabilities

- `factory-backoffice-ui`: Kit-neutral backoffice shell + workflows/agents/skills
  HTML adapters over Phase A ports (static export + thin optional serve).

### Modified Capabilities

- `factory-entity-governance`: UI must honor governance transitions and versioning
  already required by Phase A (no domain relaxation).
- `factory-contract`: Document backoffice as HTML/CLI adapter over existing
  OpenAPI paths; no second SoR schemas.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | backoffice HTML/serve, CLI, tests, kit README, OpenAPI note |

### Surface area

- Entities: none new (reuse FactoryEntity / Workflow from Phase A)
- Endpoints: kit OpenAPI descriptive note only; CLI `backoffice` + optional
  loopback HTTP adapter over the same ports (not a product API)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none required for UI; approve still uses Phase A paths
- Dependencies: stdlib only (`http.server` for optional serve)

### Write set

- `framework/ai-sdlc/aisdlc/**`
- `framework/ai-sdlc/tests/**`
- `framework/ai-sdlc/openapi/**`
- `framework/ai-sdlc/README.md`
- `docs/openspec/changes/feat-2240-factory-backoffice-shell/**`
- `docs/openspec/specs/**`
- `docs/openspec/changes/archive/**`

### Architecture review

Follows kit Ports & Adapters: HTML/CLI are driving adapters; catalogs/workflows
remain the application services. No ADR required (extends existing factory
control surface / `#2142` HTML direction). No product hexagonal slice.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document `backoffice` CLI (`--html`, optional `serve`) |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Thin description that UI adapters use existing catalog/workflow paths |
| `docs/openspec/specs/factory-backoffice-ui/spec.md` | Created on archive |
| `CHANGELOG.md` | omit Unreleased (fence / release-please) |

## Out of Scope

- Phase C modules (providers, knowledge, runs, rules, integrations) → #2241
- Phase D runtime project/run UI → #2242
- Phase E Notaire adopter demo branding → #2243
- Context Lake / Port.io clone / Wilson vanity / fake Isolator
- Notaire `frontend/` SPA / product Playwright specs
- Second config database or bypass of Phase A approve guards
- Changing `[committer] required=true`
