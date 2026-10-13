# GitHub App identity so fleet agents can write labels and board status

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1648 |
| Use Case | CU78 — Security, Privacy and Compliance |
| Branch | `feat/1648_fleet-app-identity` |
| Gate 1 status | passed |

## Objetivo

Fleet tokens got 403 on labels and Project Status (#1551). Every claim and
completion therefore needed a human handoff, and the board drifted. Autonomy
needs a fleet identity with exactly the SDLC permissions.

## What Changes

- `workspace/sdlc/fleet-token.sh`: App JWT, then an installation token, then `GH_TOKEN`. A loud fallback when the App is not configured.
- `FLEET-IDENTITY.md`: the permission table and Owner setup. Linked from the 304 README, ENVIRONMENT-CHECKLIST, PARALLEL-AGENTS and cloud-foreman.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The fleet writes GitHub only as `notaire-fleet`, with least privilege and no ruleset bypass | CU78, #1648 | New |

## Capabilities

### New Capabilities
- `fleet-identity`: App token minting with a loud fallback.

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
| `workspace/sdlc` + fleet docs | yes | script; identity doc |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: `FLEET_APP_ID`, `FLEET_APP_PRIVATE_KEY` (secret store, never committed)
- Dependencies: `openssl`, `curl`, `python3`

### Architecture review

GitHub App over a PAT: short-lived tokens, scoped to one repository, auditable as a bot.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/FLEET-IDENTITY.md` | New |
| `docs/300-development/304-ai-sdlc-cloud/README.md` | Link |
| `docs/300-development/304-ai-sdlc-cloud/ENVIRONMENT-CHECKLIST.md` | Token row |
| `docs/300-development/305-project-management/PARALLEL-AGENTS.md` | Handoff only without App |
| `.claude/agents/cloud-foreman.md` | Use the App token |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Creating and installing the App and storing secrets (**Owner action**).
