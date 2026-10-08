# Management history PUT/DELETE are ADMIN-only (#1250 bucket B)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1250 |
| Use Case | CU13 – Gestionar historial de gestion; CU78 – Seguridad |
| Branch | `fix/1250_history_writes_admin_only` |
| Gate 1 status | draft |

## Objetivo

Any authenticated user could rewrite or delete a management history row, which is the audit trail of state changes. Restrict `PUT`/`DELETE /api/v1/historial/{id}` to administrators (Owner decision on #1250 bucket B: restrict, do not remove) and record the Owner's triage of the allowlisted endpoints.

## What Changes

- `SecurityAndCorsConfig`: `PUT`/`DELETE /api/v1/historial/*` require `ROLE_ADMIN`.
- `HistoryController`: 403 response and ADMIN note in the OpenAPI docs; `openapi.yaml` regenerated.
- Bruno `rbac/08a`, `rbac/08b` (employee gets 403); `rbac/09` reordered.
- `API-AUTHENTICATION-GUIDE.md` RBAC table.
- `contracts/api-reachability-allowlist.yaml`: reasons per approved bucket (A 18, B 5, C 19, D 10, E 1).
- CHANGELOG Security entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Only an administrator rewrites or deletes history | #1250 (Owner 2026-10-07) | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `management-history`: Recording and reading the state-change history of deed managements.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `SecurityAndCorsConfig`, `HistoryController`, OpenAPI, Bruno |
| `contracts` | yes | allowlist reasons |
| `docs` | yes | API authentication guide |
| `frontend` | no | — (no screen writes history) |

### Surface area

- Endpoints: `PUT`/`DELETE /api/v1/historial/{id}` (now 403 for non-administrators; documented)
- Entities / Flyway / Configuration: none

### Architecture review

Same mechanism as the existing RBAC rules (`hasRole(ADMIN)` in the API filter chain), method-specific so `GET`/`POST` stay open to authenticated users.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Security entry |
| `backend-api/openapi/openapi.yaml` | 403 responses |
| `docs/200-architecture/206-security/API-AUTHENTICATION-GUIDE.md` | RBAC table |
| `contracts/api-reachability-allowlist.yaml` | triage reasons |
