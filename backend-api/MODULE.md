# backend-api

**Purpose:** REST API, business rules, persistence and the Flyway-managed PostgreSQL schema.

**Verify:** `bash backend-api/verify.sh` (module checks only; the full gate is `bash scripts/preflight.sh`).

## Contract (what other modules may rely on)

- `backend-api/openapi/openapi.yaml` (committed, diffed in CI); `/api/v1` endpoints
- Image `notaire-backend`; Actuator `/actuator/health` and `/actuator/prometheus`
- Flyway migrations in `src/main/resources/db/migration/`

## Seams (what this module reads from outside)

- Environment variables from `.env` (database, JWT, actuator credentials)

## Must not

- Import or depend on `frontend`, `infra`, `testing` or `local-ai`
- Write log files (logs leave as JSON on stdout)

Manifest entry: [`workspace/modules.yaml`](../workspace/modules.yaml). Rationale: ADR-026.
