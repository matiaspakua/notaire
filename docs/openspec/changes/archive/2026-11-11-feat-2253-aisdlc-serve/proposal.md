# `aisdlc serve` read-only HTTP host (Phase A.2)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2253 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `feat/2253_aisdlc-serve` |
| Gate 1 status | passed |

## Objetivo

`openapi/factory-control.openapi.yaml` declares `/factory/*` paths but the kit hosts
no OpenAPI HTTP server (`contract.py`). Phases B–D need one stdlib host that serves
the service layer honestly. Design record #2251 / `FACTORY-UI-SPEC` section 3 defines
loopback bind, Bearer token scopes, `problem+json`, and `GET /factory/capabilities`.

## What Changes

- New `aisdlc serve [--port N] [--host H] [--allow-remote]`: stdlib
  `http.server` / `wsgiref` host implementing declared **GET** factory paths.
- Bearer token file `.aisdlc/serve.token` (mode 0600, created on first run, never
  logged); scopes `read` < `write` < `owner`; this slice enforces **read only**
  and answers **405** for mutating methods.
- Bind defaults to `127.0.0.1`; non-loopback refused unless `--allow-remote` and a
  token is configured.
- Additive OpenAPI path `GET /factory/capabilities` with honest port truth
  (`approvals_live`, runner, forge write, isolator `sandbox`, `enforces_committer`).
- Static asset mount point for future kit UI; ships no SPA assets yet.
- Errors as RFC 9457 `application/problem+json` with stable `code`; id regex
  blocks path traversal; secret redaction.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| One HTTP host implements the factory OpenAPI GET surface from the service layer; CLI remains SoR | FACTORY-UI-SPEC §3; #2253 | New |
| Loopback-only default; remote bind needs `--allow-remote` + configured token | FACTORY-UI-SPEC §3 | New |
| Bearer token scopes read/write/owner; A.2 serves read only (405 on mutate) | FACTORY-UI-SPEC §3; #2253 | New |
| Capabilities report honest port truth (false when not proven) | FACTORY-UI-SPEC §3 Capabilities | New |
| Errors are problem+json with stable codes; no plaintext credentials in responses/logs | #2253 AC | New |

## Capabilities

### New Capabilities

- `aisdlc-serve`: read-only stdlib HTTP host for factory OpenAPI GET paths + capabilities.

### Modified Capabilities

- `factory-contract`: additive `GET /factory/capabilities` (+ Problem / security notes); no second OpenAPI tree.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `workspace` (harness) | yes | `framework/ai-sdlc/**`, OpenSpec, kit README |

### Surface area

- Entities: none
- Endpoints: kit OpenAPI `GET /factory/capabilities` (additive); host serves existing GET `/factory/*`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `.aisdlc/serve.token` (local, mode 0600)
- Dependencies: stdlib only (`http.server` / `wsgiref`)

### Write set

- `framework/ai-sdlc/**`
- `docs/openspec/**`
- `docs/300-development/303-testing/INVENTORY.md`
- `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md`

### Architecture review

Follows FACTORY-UI-SPEC §3 (service layer → HTTP adapter). No new ADR; ADR-003
already contemplates a future HTTP adapter over the published contract.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `framework/ai-sdlc/README.md` | document `aisdlc serve`, token, loopback rules |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | add capabilities path (+ problem schema as needed) |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-UI-SPEC.md` | note A.2 host shipped when done (light) |
| `docs/300-development/303-testing/INVENTORY.md` | register new kit tests |
| `CHANGELOG.md` | n/a — omit if Unreleased fence held (release-please) |

## Out of Scope

- Writes / If-Match / workflow body v2 (#2254 tip — do not steal).
- Project / Run entities (#2255).
- Phase D SPA / Context Lake / Wilson vanity / fake Isolator.
- Changing `[committer] required=true`.
- Competing OpenAPI tree or product Playwright.
