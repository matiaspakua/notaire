# Deny HTTP mutations on audit-log (#1060 residual)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1060 |
| Use Case | CU23 – Ver registro de actividades de usuario; CU78 – Security and Compliance; CU73 – Registro de Auditoría |
| Branch | `cursor/fix-1060-audit-log-mutations-69d3` |
| Gate 1 status | passed |

## Objetivo

Issue #1060 requires that clients cannot forge or alter audit-log rows over HTTP.
`POST /api/v1/audit-log` was already removed on `main` by #1124 (#1068). Residual
acceptance remains: prove PUT/DELETE are denied (405), add Bruno negative cases,
and fix the OpenAPI tag that still says “administrar”.

## What Changes

- Keep `AuditRecordController` GET-only (no re-removal of POST; already gone).
- Extend unit + integration tests so PUT and DELETE on `/api/v1/audit-log` return **405**.
- Add Bruno/api-test negative requests (POST/PUT/DELETE expect 405) under `audit-records/`.
- Update OpenAPI `@Tag` description to consult-only; ensure no create operation in Swagger.
- Document in the threat model that the HTTP forge vector is closed (SR-07 adjacent).
- Close #1060 via PR (`Closes #1060`).

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Clients MUST NOT create, update, or delete audit rows over HTTP | CU73 / CU78 / #1060 AC | Made explicit (PUT/DELETE + contract tests) |
| Audit rows MUST be written only by server-side `AuditAspect` from JWT identity | CU73 / SR-07 | Unchanged (already Mitigated) |
| HTTP mutations on `/api/v1/audit-log` MUST return **405 Method Not Allowed** | #1060 AC (404 or 405 allowed; lock 405) | Made explicit |

## Capabilities

### New Capabilities

- `audit-log-mutation-deny`: Public audit-log HTTP surface is read-only; POST/PUT/DELETE are rejected with 405; OpenAPI and Bruno reflect consult-only access.

### Modified Capabilities

- None (capability is new residual AC closure; #1068 already added `api-request-dto-binding` with POST-only deny).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | OpenAPI tag; unit/integration tests; Bruno negatives |
| `frontend` | no | Already GET-only via `useAuditoria` |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `AuditRecord` / `registro_auditoria` — no schema change
- Endpoints: `POST`/`PUT`/`DELETE` `/api/v1/audit-log` and `…/{id}` remain unmapped → 405
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** none beyond POST already removed in #1124

### Architecture review

Follows existing hexagonal adapter (`adapter.in.web.audit.AuditRecordController`) and
server-side writer (`AuditAspect`). No ADR required — residual security AC closure,
not an architectural change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/206-security/THREAT-MODEL-AUTH-AUDIT.md` | Note under SR-07 (or adjacent) that HTTP forge via POST/PUT/DELETE is closed on #1060 |
| `backend-api` OpenAPI `@Tag` on `AuditRecordController` | Consult-only wording |
| `backend-api/api-test/COVERAGE.md` | Mention mutation-deny Bruno cases |
| `CHANGELOG.md` | Brief Unreleased note that #1060 residual AC (PUT/DELETE deny + Bruno/OpenAPI) closed |
| `docs/github/PRODUCTION-READINESS-AUDIT-2026-09.md` | Mark #1060 closed when merged (optional follow-up) |

## Out of Scope

- Full GET RBAC for audit viewers (#559 / SR-06).
- DB append-only / SR-09 tamper evidence.
- Auditing GET reads (SR-08).
- Deleting legacy `AuditRecordJpaController`.
- Re-removing POST (already done in #1124).
