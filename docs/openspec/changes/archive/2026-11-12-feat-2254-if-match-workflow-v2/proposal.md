# Phase A.3 — If-Match writes, schema_version, workflow body v2

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2254 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2254-if-match-workflow-v2-55fb` |
| Gate 1 status | passed |

## Objetivo

Workflows still store bare id lists, references are not pinned, writes carry no
expected version, entities lack `schema_version`, and transitions record no
server-derived actor (F3, F5, F6, F9 in `FACTORY-PRODUCT-ANALYSIS`). Phase B2
governance writes and honest approve require A.3 contract hardening on the kit
service layer and `aisdlc serve` write path.

## What Changes

- Every catalog entity envelope gains `schema_version`, `created_at`, and
  server-derived `created_by` (channel + identity). Request bodies cannot set
  `created_by`.
- `aisdlc migrate` upgrades workflow body schema v1 → v2 idempotently and
  appends a migration record to the evidence ledger.
- Workflow body v2: `steps[]` (`id, stage, agent, skills[], mcp[], provider,
  gates[], budget{tokens,time_seconds,usd}, on_fail`), `inputs[]`, `outputs[]`;
  refs as `<kind>:<id>@<version>`.
- `request_validation` resolves and pins every ref (digest recorded);
  `approve` fails if a pinned digest no longer matches; body edits via
  `new_version` return governance to `draft`.
- Catalog/CLI update and `PUT /factory/catalogs/{kind}/{id}` require expected
  version (`If-Match`); mismatch → 409 problem with current version; two-writer
  test proves no lost update.
- Mutating HTTP requires Bearer `write` (approve Owner override needs `owner`),
  CSRF header, and `aisdlc serve --write`.
- OpenAPI stays one tree; additive If-Match / schema fields / workflow v2 body.
- **PRESERVE:** v1 CLI reads and Phase A governance / entity-versioning tests
  stay green unmodified.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every entity carries `schema_version`; migrate upgrades workflow v1→v2 idempotently and records evidence | #2254 AC; FACTORY-UI-SPEC §5; F9 | New |
| Workflow body v2 uses steps/inputs/outputs and pinned `<kind>:<id>@<version>` refs | #2254 AC; FACTORY-UI-SPEC §5; F3 | New |
| `request_validation` pins digests; `approve` refuses digest drift; new version → `draft` | #2254 AC; FACTORY-UI-SPEC §5 | New |
| Writes require expected version (`If-Match`); mismatch → 409 with current version | #2254 AC; F5 | New |
| `created_by` is server-derived from token identity + channel; body cannot set it | #2254 AC; F6; axiom 4 | New |
| Mutating HTTP needs write scope, CSRF header, and `--write`; Owner approve needs `owner` | #2254 AC; FACTORY-UI-SPEC §3 | New |
| CLI v1 outputs unchanged until migrated (PRESERVE goldens / governance tests) | #2254 AC | Made explicit |

## Capabilities

### New Capabilities

- `aisdlc-migrate`: idempotent catalog/workflow schema migration with evidence.

### Modified Capabilities

- `factory-entity-governance`: workflow body v2, pinned refs, digest-guarded approve, actor audit, If-Match writes.
- `aisdlc-serve`: opt-in mutating PUT with If-Match, CSRF, write/owner scope (no longer always 405 when `--write`).
- `factory-contract`: additive OpenAPI for If-Match, `schema_version`, workflow v2 body schemas.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | `framework/ai-sdlc/**`, OpenSpec, kit docs |

### Surface area

- Entities: catalog envelope + workflow body schema
- Endpoints: kit OpenAPI PUT catalogs (If-Match); serve implements writes when `--write`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: serve `--write`; optional actor identity in token file
- Dependencies: stdlib only

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/306-ai-sdlc-framework/**` (pointer notes only if needed)
- `docs/300-development/303-testing/INVENTORY.md`
- `CHANGELOG.md` — omit Unreleased if fence held (#2271)

### Out of scope / Refuse

- Projects / Runs (A.4 #2255)
- Phase B–D SPA chrome; Context Lake / Port.io clone; Wilson vanity; fake Isolator
- Changing committer `required=true`
- Continuing or force-pushing stale tip `feat/2254_workflow-v2-schema-pins-migrate`

## Cross-Module Coordination

None — kit-only.

## Compatibility & Migration Plan

- Existing v1 workflow bodies remain readable until `aisdlc migrate`.
- Migrate is idempotent; second run is a no-op with evidence note.
- PRESERVE: `test_workflow_governance.py`, `test_entity_versioning.py`, CLI goldens for v1.

## Documentation Impact

| Document | Action |
|----------|--------|
| `framework/ai-sdlc/README.md` | Document `migrate`, If-Match / `--write` serve |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | Additive If-Match + schema_version + v2 body |
| `docs/300-development/303-testing/INVENTORY.md` | New kit tests |
| `FACTORY-UI-SPEC.md` | Already normative; no rewrite unless drift |
| `CHANGELOG.md` | Omit if Unreleased fence held |

## Open Questions

None — design record #2251 / FACTORY-UI-SPEC sections 3–5 are authoritative.
