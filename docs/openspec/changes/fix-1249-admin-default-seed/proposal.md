# Skip the admin/admin seed outside dev/test (#1249)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1249 |
| Use Case | CU78 – Autenticación; CU84 – Gestión de credenciales |
| Branch | `fix/1249_admin_default_credentials_seed` |
| Gate 1 status | draft |

## Objetivo

Stop any non-development launch path from creating an administrator with the known `admin` password.

## What Changes

- `DataInitializer` reads `app.environment` and skips the seed, logging an error, when the password is blank or `admin` and the environment is not `development`, `dev`, `local` or `test`.
- ADR-019, `.env.example` and `CHANGELOG.md` document the rule.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The seeded admin never uses the known default password outside dev/test | #1249 | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `admin-bootstrap`: Initial administrator seeding at startup.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `DataInitializer` |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| Docs / scripts / CI | yes | ADR-019, `.env.example`, CHANGELOG |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration: reads existing `app.environment`

### Architecture review

Follows the `ProductionCredentialsGuard` pattern; no ADR change beyond documenting the rule in ADR-019.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one Fixed entry |
| `docs/200-architecture/202-ADR/ADR-019-secrets-management.md` | runtime enforcement section |
| `.env.example` | comment on APP_ADMIN_PASSWORD |
