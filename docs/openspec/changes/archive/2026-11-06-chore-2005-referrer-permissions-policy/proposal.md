# Add Referrer-Policy and Permissions-Policy headers

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2005 |
| Use Case | CU76 |
| Branch | `chore/2005_referrer-permissions-policy` |
| Gate 1 status | passed |

## Objetivo

Production Next.js responses set frame/options/HSTS headers but not Referrer-Policy or Permissions-Policy.

## Why

Issue #2005: browsers still send full referrers and allow powerful APIs by default unless these headers are set.

## What Changes

Add `Referrer-Policy: strict-origin-when-cross-origin` and a restrictive `Permissions-Policy` to `frontend/next.config.ts` static headers. Extend the Vitest security-headers suite (TDD).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| App responses include Referrer-Policy and Permissions-Policy. | Issue #2005; CU76 | New |

## Capabilities

### New Capabilities

- `frontend-security-headers`: Referrer-Policy + Permissions-Policy on all routes.

### Modified Capabilities

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | — |
| `frontend` | yes | next.config.ts + Vitest |
| `frontend-swing` | no | — |
| `infra` / observability | yes | — |
| CI/CD (`.github/workflows`) | yes | — |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Architecture review

Follows existing static headers pattern (#562); no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Unreleased security note for #2005 |

## Out of Scope

["Changing CSP nonce proxy", "Backend Spring Security headers"]
