# Enforce role-based authorization on the API (CU78)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #559 |
| Use Case | CU78 – Security, Privacy and Compliance |
| Branch | `fix/559_rbac_enforcement` |
| Gate 1 status | draft |

## Objetivo

Every authenticated user can call every endpoint: an EMPLEADO can list users and create an ESCRIBANO account. The admin restriction exists only in the frontend. Enforce it on the server for the administrative API families.

## What Changes

- The JWT filter resolves the authority from the stored user (type and active flag) on each request: `ROLE_ADMIN` for Administrador, Admin or Escribano, `ROLE_USER` otherwise; unknown or inactive users are not authenticated.
- `/api/v1/usuarios` (except login and logout), `/api/v1/roles` and `/api/v1/audit-log` require `ROLE_ADMIN`.
- Mutations of workflow definitions, nodes and transitions and of the administrative catalogs require `ROLE_ADMIN`; reads stay open to authenticated users.
- 403 responses come from one access-denied handler next to the existing 401 entry point.
- The frontend treats `/dashboard/auditoria` as an administrator route.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Only administrator-capable users manage users, roles, the audit log and the administrative catalogs | CU78, #559 | Made explicit |
| A token of a deleted or inactive user is rejected | CU78, #559 | Made explicit |

## Capabilities

### New Capabilities

- `api-authorization`: Server-side authorization of the administrative API families.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | authority resolver, filter, security chain, access-denied handler |
| `frontend` | yes | admin route list |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | ADR-008, authentication guide, Bruno, E2E |

### Surface area

- Endpoints: no new ones; 403 added for non-administrators on the listed families (BREAKING for non-admin API clients of those endpoints)
- Entities / Flyway / Configuration: none
- Dependencies: none

### Architecture review

Extends ADR-008 (security authentication) with an authorization section; no schema change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-008-security-authentication.md` | authorization section |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | roles and 403 behaviour |
| `CHANGELOG.md` | one entry |
