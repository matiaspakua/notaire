# security: check a Threat Dragon model against the STRIDE register

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2009 |
| Use Case | CU78 — Seguridad, Privacidad y Cumplimiento |
| Branch | `cursor/2009-threat-dragon-47dc` |
| Gate 1 status | passed |

## Objetivo

The STRIDE register and a diagram model could drift, and CI had no check that a Threat Dragon file was a real v2 model. The upstream schema is checkable without the Dragon binary.

## What Changes

- Check in a v2 model whose threat ids are the register's `SR-*` ids.
- Vendor the upstream v2 schema (commit `30dc0ff`) and validate the model against the keywords that schema uses.
- The test fails when an id is only in the register or only in the model.
- Threat Dragon leaves the "not adopted" list. pytm and Threagile stay there.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every register `SR-*` id is a threat id in the Dragon model, and the reverse | CU78, #2009 | New |
| The model has no secrets and no exploit procedure | CU78, #2009 | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP API does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD | no | The check is a local unittest inside preflight |

### Surface area

- Endpoints: none
- Not **BREAKING**.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/SECURITY-TOOLS.md` | Adopt the model |
| `docs/200-architecture/206-security/threat-model/system.stride.yml` | Drop Threat Dragon from the not-adopted list |
| `docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md` | Gap row |
| `docs/200-architecture/206-security/CYBERSECURITY-PROGRAM.md` | Open-work table |

## Out of Scope

- Running the Threat Dragon application.
- pytm or Threagile.
- Editing the GitHub Pages site.
