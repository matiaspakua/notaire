# Dependabot skips typescript major in /frontend

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1883 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `build/1883_dependabot_ignore_ts_major` |
| Gate 1 status | pa## Objetivo

Dependabot PR #1871 (typescript 5.9.3 → 7.0.2) failed: `openapi-typescript@7.13.0`
has peer `typescript@^5.x` (npm ERESOLVE), and TS7 ships no compiler API
(`createProgram`), which Next.js type-check, openapi-typescript and
eslint-config-next use. The Owner closed it and approved an ignore rule.

## What Changes

- `.github/dependabot.yml`, npm `/frontend`: ignore `typescript` `version-update:semver-major`. Minor/patch stay grouped.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Toolchain majors that break the build are planned changes, not bot PRs | #1621 / #1643 precedent, Owner approval | Made explicit for typescript |

time |

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none; dependency-bot configuration only (`skip_specs: true`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | – |
| `frontend` | no | – |
| `infra` / observability | no | – |
| CI/CD (`.github`) | yes | `dependabot.yml` ignore rule; test in `security/tests` |
| `workspace` | no | – |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Configuration only; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Changed entry |

## Out of Scope

- Migrating to TypeScript 7 (new issue when the tooling supports it).
