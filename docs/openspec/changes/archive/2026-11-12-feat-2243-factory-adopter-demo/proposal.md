# Phase E — Notaire adopter wiring / demo path for factory UIs

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2243 |
| Use Case | CU76 — factory control |
| Branch | `cursor/2243_factory-adopter-demo-2731` |
| Gate 1 status | passed |

## Objetivo

Phases A–D shipped kit-capable factory serve / backoffice / runtime UIs. Operators
and adopters still lack a **proven demo path**: at least one **approved** workflow
whose agents/skills refs match Foreman stage knobs, documented commands + UI entry,
and a kit-neutral second-adopter fixture that keeps independence green — without
replacing Notaire product admin or branding the kit as Notaire paths.

## What Changes

- Kit-neutral `aisdlc demo seed` (optional `--from-stages`) that ensures catalog
  entries for agents/skills, creates one workflow, migrates to body v2, and
  advances governance to `approved` via Owner override when approvals are not live
- Second-adopter fixture exercised in kit tests (temp project; no adopter product
  paths in kit code/templates)
- Notaire adopter profile catalogs: skills refs + one approved workflow pinned to
  agents/skills already used by Foreman `[[stages]]`
- Permanent demo docs (commands + UI entry) under kit + `306-ai-sdlc-framework`
- CU76 honesty: `catalog validate`, `doctor`, and `report` stay green after seed
- Kit independence test still passes; no Notaire product admin replace; no product
  Playwright tip

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Adopter demo exposes ≥1 approved workflow with pinned agents/skills used by Foreman stages | #2243 AC / FACTORY-UI-SPEC Phase E | New |
| Demo path is documented (CLI commands + UI entry for serve/backoffice/runtime) | #2243 AC | New |
| Kit independence holds for a temp second-adopter project; kit code names no adopter product paths | #2243 AC / CU76 / F1 | Made explicit for demo |
| Notaire product embedding is optional; never replace product admin with factory backoffice | #2243 AC / Owner refuse | Made explicit |
| `validate` / `doctor` / `report` remain green after demo seed; evidence verify honest | #2243 AC / CU76 | Made explicit |
| Leave `[committer] required=true` alone; no Context Lake / Wilson vanity / fake Isolator | Owner refuse | Made explicit |

## Capabilities

### New Capabilities

- `factory-adopter-demo`: Kit-neutral demo seed + documented adopter demo path;
  Notaire sandbox wiring of catalogs/workflows for factory UIs.

### Modified Capabilities

- `factory-runtime-ui`: Document that Start-run demo depends on an approved
  workflow from the adopter demo path (no new runtime UI surface).
- `factory-entity-governance`: Adopter profiles may now ship an approved workflow
  for demo (previously deferred to Phase E).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` / `framework/ai-sdlc` | yes | demo seed, CLI, kit tests, kit docs |
| `.aisdlc` (Notaire adopter) | yes | skills catalog + approved workflow |
| `docs/300-development/306-ai-sdlc-framework` | yes | FACTORY-ADOPTER-DEMO + Phase E note |

### Surface area

- Entities: none new (reuse catalogs + workflows governance)
- Endpoints: no new OpenAPI paths; demo uses existing CLI / serve / UI adapters
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: stdlib only

### Write set

- `framework/ai-sdlc/aisdlc/demo.py`
- `framework/ai-sdlc/aisdlc/cli.py`
- `framework/ai-sdlc/tests/test_adopter_demo.py`
- `framework/ai-sdlc/tests/test_notaire_adopter_demo.py`
- `framework/ai-sdlc/README.md`
- `framework/ai-sdlc/MODULE.md`
- `framework/ai-sdlc/docs/ADOPTER-DEMO.md`
- `.aisdlc/catalogs/skills/**`
- `.aisdlc/catalogs/workflows/**`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-ADOPTER-DEMO.md`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md`
- `docs/openspec/changes/feat-2243-factory-adopter-demo/**`
- `docs/openspec/specs/factory-adopter-demo/spec.md` (on archive)

### Architecture review

Follows kit Ports & Adapters: demo seed is an application helper over catalogs /
workflows ports; HTML UIs remain adapters. No ADR. Does not invent a second
config truth. Public wheel second-adopter sample + conformance matrix remain
#2256 packaging remainders (fixture in kit tests is the Phase E proof).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/docs/ADOPTER-DEMO.md` | New: kit-neutral demo commands + UI entry |
| `framework/ai-sdlc/README.md` | Link demo path / `aisdlc demo` |
| `framework/ai-sdlc/MODULE.md` | Contract: demo seed |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ADOPTER-DEMO.md` | New: Notaire sandbox demo + UI entry |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | Phase E shipped note |
| `CHANGELOG.md` | omit Unreleased (fence / #2271) |

## Out of Scope

- Replacing Notaire product admin with factory backoffice
- Public sample repo + wheel time-to-first-gate (#2256 remainder)
- Conformance matrix CI job (#2256)
- Context Lake / Port.io clone / Wilson vanity / fake Isolator
- Changing `[committer] required=true`
- Contending with MFA docs tip #2271
- Notaire product Playwright mega-PR
