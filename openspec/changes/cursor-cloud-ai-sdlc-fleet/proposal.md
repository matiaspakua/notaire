# Cursor Cloud AI SDLC foreman fleet

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1114 |
| Use Case | none — internal process/tooling for Cursor Cloud agents; no business behavior (precedent #1074 / #973 / #1027); closest area CU76 |
| Branch | `cursor/ai-sdlc-cloud-fleet-6890` |
| Gate 1 status | passed |

## Objetivo

Notaire needs a Cursor Cloud–native foreman + specialist agent fleet so autonomous
issue→PR→merge SDLC can run without the macOS `local-ai/` runtime. This change
versions the fleet architecture, agent definitions, environment checklist, and
validation plan so the cloud path is reviewable and ready before the first product issue.

## What Changes

- Add `docs/300-development/304-ai-sdlc-cloud/` (architecture, fleet manifest, env checklist, validation plan).
- Add Cursor Cloud agent defs: `cloud-foreman`, `openspec-planner`, `backend-implementer`,
  `frontend-design`, `testing-qa` under `.claude/agents/`.
- Index the fleet in `AGENTS.md` and development docs navigation.
- Explicitly exclude `local-ai/` from the cloud execution path.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| No business rule — internal agent/process tooling with no user-facing or business-data behavior. Documented exception per CONSTITUTION.md (precedent #1074 / #973 / #1027). | n/a | n/a |

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
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `.claude/agents/` | yes | New cloud fleet agent definitions |
| `docs/300-development/` | yes | New `304-ai-sdlc-cloud/` docs |
| `AGENTS.md` | yes | Cloud fleet index |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none committed; checklist references `.env.example` keys for Cloud env only
- Dependencies: none added to Maven/npm builds

### Architecture review

No change to the application architecture. Agents load existing skills and Constitution
rules; no ADR required. Cloud fleet must not invoke `local-ai/sdlc/foreman.sh`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/304-ai-sdlc-cloud/*` (new) | Fleet architecture, manifest, env checklist, validation plan |
| `AGENTS.md` | Index cloud foreman + specialists |
| `docs/README.md`, `docs/300-development/README.md` | Navigation links |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Implementing product GitHub issues via the fleet (blocked until validation GO).
- Extending or running the `local-ai/` harness.
- Authoring the Cloud `environment.json` install script (sibling env-mapping task).
