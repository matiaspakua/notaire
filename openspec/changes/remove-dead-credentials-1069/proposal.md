# Remove Dead Default Credentials from Configuration

## Why

Remove dead default security credentials (`spring.security.user.*`) and the misleading
"admin/admin" comment about Prometheus scraping from backend resources to eliminate
security risks and avoid confusing developers with obsolete configuration.

## Objetivo

Remove dead default security credentials and legacy Swing-era configuration from
backend resources.

## What Changes

- Remove lines 92-94 from `application.properties`:
  - The comment `# Prometheus will use admin/admin for scraping`
  - The dead keys `spring.security.user.name=admin`
  - The dead keys `spring.security.user.password=admin`
  - The dead keys `spring.security.user.roles=ACTUATOR,ADMIN`
- Delete `config.properties` entirely.

No other behavior changes.

## Reglas de negocio

None. This is a cleanup of unused configuration; it removes obsolete data without
altering any running business logic.

## Capabilities

### New Capabilities

### backend-resources-hygiene

Configuration file hygiene - removes dead default credential keys from application
resource files.

#### ApplicationPropertiesHygiene

Application startup properties must not contain dead default security user keys.

#### ApplicationResourceHygiene

Application resources must not include dead configuration files (config.properties).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|-------------|
| `backend-api` | yes | Remove dead keys from application.properties, delete config.properties |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: none
- Endpoints: none added/changed
- Database (Flyway `V{n}`): none
- Configuration / `.env`: dead keys removed, no new secrets added
- Dependencies: none
- Breaking: **No breaking changes** — dead credentials are not being used at runtime

### Architecture review

Not architectural. Configuration cleanup. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| CHANGELOG.md | chore(security): remove dead default credentials |

No other permanent documentation references.

## Out of Scope

- Actual datasource credentials — these are securely read from `.env` and
  validated by `ProductionCredentialsGuard` at startup (Issue: CU78 sub-task)
- Spring Security principal configuration — the application authenticates
  users via JWT tokens, not hardcoded principals
- Prometheus metrics collection — uses `management` properties, not `spring.security.user.*`

---

| Field | Value |
|-------|-------|
| GitHub Issue | #1069 |
| Use Case | CU78 — Security, Privacy and Compliance |
| Branch | `chore/1069_remove_dead_credentials` |
| Gate 1 status | passed |
