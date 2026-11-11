# Phase C — remaining factory backoffice modules + runs monitor

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2241 |
| Use Case | CU76 — factory control |
| Branch | `feat/2241_factory-backoffice-modules` |
| Gate 1 status | passed |

## Objetivo

Phase B (#2240/#2267) shipped the kit-neutral backoffice shell for workflows,
agents, and skills. Operators still lack CRUD/versioning UI for the remaining
catalog kinds (tools/MCP, providers, knowledge, rules/gates, integrations) and
a runs monitor wired to evidence/`aisdlc report` — without inventing a second
metrics store or rewriting the OpenAPI write contract owned by A.2/A.3.

## What Changes

- Extend Phase B `aisdlc backoffice` HTML/serve path with modules: **Tools/MCP**,
  **Providers**, **Knowledge** (`memory`), **Rules/Gates**, **Integrations**,
  and **Runs**
- Catalog modules: list/detail/create/edit + version history via existing Phase A
  `catalogs` ports only (same adapters as agents/skills)
- Runs module: read/monitor from evidence ledger + `summarize`/`report`/cost;
  cancel/retry **disabled with honest reason** until Runner/Forge ports + A.4
  Run entities honestly support them
- Measure/tune hooks reuse TelemetryPort / evidence cost — **no parallel metrics store**
- Keep one right-nav composition; kit-neutral brand; secrets redacted
- Do **not** steal A.2 `#2253` / A.3 `#2254` OpenAPI write/serve contracts —
  kit HTML adapters over CLI/ports first
- Kit unittest only — no Notaire product Playwright / second OpenAPI tree

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Remaining catalog modules are adapters over Phase A catalogs only | CU76 / #2241 | New |
| Runs monitor reads evidence + report/cost; no parallel metrics store | CU76 / #2241 / #2142 | New |
| Cancel/retry offered only when ports honestly support them; else read-only + empty actions with reason | CU76 / #2241 / FACTORY-UI-SPEC | New |
| Right-nav stays one composition; kit-neutral brand; no Ocean/card sprawl | Owner plan / #2241 | New |
| Secrets/credentials never plaintext in HTML | factory redaction / #2139 | Made explicit for new modules |
| No second OpenAPI SoR; A.2/A.3 tips own HTTP write contract | #2253 / #2254 / #2241 | New |
| Leave `[committer] required=true` alone | Owner refuse | Made explicit |

## Capabilities

### New Capabilities

- _(none — Phase C extends the existing backoffice capability)_

### Modified Capabilities

- `factory-backoffice-ui`: Add remaining catalog modules + runs monitor to the
  Phase B shell (nav, CRUD adapters, evidence-backed runs view, honest empty
  cancel/retry).
- `factory-contract`: Document Phase C as HTML/CLI adapters over existing catalog
  + evidence/report surfaces; no new SoR schemas / no competing OpenAPI write tree.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | backoffice modules, HTML, tests, kit README |

### Surface area

- Entities: none new (reuse catalog kinds + evidence ledger; A.4 owns Run entities)
- Endpoints: no OpenAPI path changes; optional loopback `backoffice serve` form
  actions for catalog modules only
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: stdlib only

### Write set

- `framework/ai-sdlc/aisdlc/backoffice.py`
- `framework/ai-sdlc/aisdlc/backoffice_html.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/aisdlc/evidence.py`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/MODULE.md`
- `framework/ai-sdlc/tests/test_backoffice_html.py`
- `framework/ai-sdlc/tests/test_backoffice_serve.py`
- `framework/ai-sdlc/tests/test_backoffice_phase_c.py`
- `docs/openspec/changes/feat-2241-factory-backoffice-modules/**`
- `docs/openspec/specs/factory-backoffice-ui/spec.md`
- `docs/openspec/specs/factory-contract/spec.md`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md`

### Architecture review

Extends Phase B Ports & Adapters HTML shell (ADR-030 kit pattern / existing
`report --html`). No new ADR. Does not implement `aisdlc serve` (#2253) or
If-Match writes (#2254).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document Phase C modules + runs monitor |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | Note Phase C shipped via backoffice HTML adapters |
| `docs/openspec/specs/factory-backoffice-ui/spec.md` | Sync on archive |
| `docs/openspec/specs/factory-contract/spec.md` | Sync adapter note on archive |
| `CHANGELOG.md` | omit if Unreleased fence held |

## Out of Scope

- Phase D runtime project instantiation UI (#2242)
- A.2 `aisdlc serve` OpenAPI host (#2253) and A.3 If-Match / workflow body v2 (#2254)
- A.4 Project/Run/RunRequest entities (#2255) — runs UI stays evidence-derived
- Wilson vanity stages; fake Isolator; Context Lake / Port.io Ocean
- Changing `[committer] required=true` / `required=false` Owner knobs
