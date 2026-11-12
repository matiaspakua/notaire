# Phase D — factory runtime UI (project, repos, approved workflow, progress)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2242 |
| Use Case | CU76 — factory control |
| Branch | `cursor/2242_factory-runtime-ui-201b` |
| Gate 1 status | passed |

## Objetivo

Operators need a **runtime** surface distinct from the backoffice: create a
project, bind repositories, select an **approved** workflow, start a run, and
monitor evidence-derived progress. Phase A.4 (#2255/#2274) shipped Project /
Run / RunRequest; B/C shipped control-plane HTML. Phase D closes the Owner
runtime UX over those ports without inventing a second SoR or faking `running`.

## What Changes

- New kit-only runtime UI adapters: `aisdlc runtime --html|--serve` over
  `projects` / `runs` / approved `workflows` ports (stdlib HTML; no Node build)
- D1: create project (profile root binding) + bind repos (ForgePort-honest refs;
  `dry_run` default)
- D2: workflow picker lists **approved** only; start run creates RunRequest;
  refuse non-approved
- D3: progress monitor from Run entity + evidence events; never paint `running`
  without harness events; skip ≠ pass; cancel honestly disabled / unavailable
- Agent never commits or approves from this UI
- Adapter only — reuse A.4 OpenAPI / CLI / serve paths; no second OpenAPI tree
- Kit unittest only; no Notaire product Playwright mega-PR

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Runtime UI is separate from backoffice (Projects / Start run / Monitor) | FACTORY-UI-SPEC §6 / #2242 | New |
| Create project binds profile root (file/profile; not SaaS tenancy) | #2242 AC / A.4 | Made explicit for UI |
| Bind repos with ForgePort-honest refs; mode defaults `dry_run` | #2242 AC / A.4 | Made explicit for UI |
| Workflow picker shows only governance `approved` | #2242 AC | New |
| Start run + monitor from evidence; never fake `running` without events; skip ≠ pass | #2242 AC / FACTORY-UI-SPEC §5 | Made explicit for UI |
| Agent never commits or approves from runtime UI | #2242 AC / axioms | Made explicit |
| Adapters over ports/CLI/OpenAPI / `aisdlc serve` only | #2242 AC / CU76 | New |
| Leave `[committer] required=true` alone; no Context Lake / Wilson / fake Isolator | Owner refuse | Made explicit |

## Capabilities

### New Capabilities

- `factory-runtime-ui`: Kit-neutral runtime HTML/serve for Projects, Start run,
  and evidence-honest Monitor over Phase A.4 Project/Run ports.

### Modified Capabilities

- `factory-contract`: Document Phase D as HTML/CLI adapters over existing
  Project/Run/workflow OpenAPI paths; no second SoR / no competing schemas.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | runtime adapters, HTML, CLI, tests, kit README / 306 docs |

### Surface area

- Entities: none new (reuse A.4 Project / Run / RunRequest)
- Endpoints: no new OpenAPI paths; descriptive note that runtime UI adapts
  existing `/factory/projects` and `/factory/runs`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: stdlib only

### Write set

- `framework/ai-sdlc/aisdlc/runtime.py`
- `framework/ai-sdlc/aisdlc/runtime_html.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/ui/tokens.css` (optional shared tokens if extracted)
- `framework/ai-sdlc/ui/README.md`
- `framework/ai-sdlc/openapi/factory-control.openapi.yaml` (descriptive note)
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/MODULE.md`
- `framework/ai-sdlc/tests/test_runtime_html.py`
- `framework/ai-sdlc/tests/test_runtime_serve.py`
- `docs/openspec/changes/feat-2242-factory-runtime-ui/**`
- `docs/openspec/specs/factory-runtime-ui/spec.md` (on archive)
- `docs/openspec/specs/factory-contract/spec.md` (on archive)
- `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md`

### Architecture review

Sibling of Phase B/C Ports & Adapters HTML shell (ADR-030 kit pattern). Runtime
area stays separate from backoffice MODULES per FACTORY-UI-SPEC §6. No new ADR.
Does not invent Project/Run storage (A.4 owns that). Prefer loopback
`runtime --serve` calling the same `projects`/`runs` functions as CLI/OpenAPI
(B/C shipping pattern). Deviation from mounting only under `aisdlc serve /ui/`:
mechanical — match B/C velocity; still adapters over the same ports.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | Document `aisdlc runtime --html/--serve` |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | Note Phase D shipped via kit runtime HTML adapters |
| `docs/openspec/specs/factory-runtime-ui/spec.md` | Sync on archive |
| `docs/openspec/specs/factory-contract/spec.md` | Sync adapter note on archive |
| `CHANGELOG.md` | omit if Unreleased fence held (#2271) |

## Out of Scope

- Phase E Notaire adopter demo (#2243)
- Hosted compute lock-in · Context Lake · Wilson vanity · fake Isolator
- Auto-merge without written merge rule · `[committer] required=true`
- MFA docs tip #2271
- Notaire product Playwright / second OpenAPI tree
- Agent commit/approve affordances in the UI
