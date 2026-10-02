# Cursor Cloud Agent environment scripts

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1115 |
| Use Case | CU76 — Engineering Constitution process tooling (closest area; no product CU) |
| Branch | `cursor/cloud_agent_environment` |
| Gate 1 status | passed |

## Objetivo

Cursor Cloud Agent VMs need reproducible install/start scripts and a Docker
host-network override so nested Docker can bring Notaire (Postgres + backend +
frontend) to a healthy state for AI SDLC work. This change versions those scripts
so `sdlc-process.yml` Gate 1 can pass and the cloud environment can wire them.

## What Changes

- Add `.cursor/install.sh` — idempotent toolchain/bootstrap for Cloud Agents.
- Add `.cursor/start.sh` — starts the stack for AI SDLC sessions.
- Add `docker-compose.cloud.yml` — host-network override (nested Docker bridge
  networking fails between containers).
- Add this OpenSpec change folder (`skip_specs: true`) so Process Checks pass.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — internal cloud/process tooling with no user-facing or business-data behavior. Documented exception per CONSTITUTION.md (precedent #1074 / #1114). | CU76 area | Made explicit |

## Capabilities

### New Capabilities

None — no product behavior changes. `skip_specs: true` is set in `.openspec.yaml`.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `.cursor/` | yes | New `install.sh` / `start.sh` |
| `docker-compose.cloud.yml` | yes | Host-network override for nested Docker |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: scripts read existing `.env` / `.env.example`; none hard-coded
- Dependencies: install script may ensure host packages (`bc`, Docker tooling); no Maven/npm dependency changes

### Architecture review

No application architecture change. Compose override is Cloud-only and must not
replace the default `docker-compose.yml` for local developer use. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Cloud environment docs / checklist (sibling fleet PR #1111) | May reference these scripts once merged |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Fleet agent definitions and `docs/300-development/304-ai-sdlc-cloud/` (PR #1111).
- Product GitHub issue implementation.
- Extending or running the `local-ai/` harness.
