# ADR-019: Secrets Management

## Status
Accepted

## Context
Both the application stack (`docker-compose.yml`) and the observability
stack (`infra/docker-compose.yml`) need credentials (database, JWT signing
key, Actuator/Grafana/SonarQube admin accounts, a least-privilege Postgres
metrics-exporter role). A single, consistent approach was needed instead of
scattering secrets across multiple compose files or hardcoding them.

## Decision
All credentials live in a **single, git-ignored `.env` file at the repo
root**, seeded from a checked-in `.env.example` template. Both compose files
read from this one file — there is no separate `infra/.env`.

### What's in `.env`
| Variable(s) | Purpose |
|-------------|---------|
| `POSTGRES_DB/USER/PASSWORD` | Application database |
| `PGADMIN_DEFAULT_EMAIL/PASSWORD` | pgAdmin |
| `APP_ADMIN_USER/PASSWORD` | Seeded admin user (stored as BCrypt hash in DB, issue #554; seeded once on first startup, issue #651) |
| `JWT_SECRET` | Token signing key — **mandatory**, backend refuses to start if blank, &lt;32 bytes, or left as the example value |
| `ACTUATOR_USER/PASSWORD` | HTTP Basic auth for `/actuator/prometheus`, also referenced by `infra/prometheus/prometheus.yml`'s `basic_auth` block |
| `GRAFANA_ADMIN_USER/PASSWORD` | Grafana |
| `POSTGRES_EXPORTER_USER/PASSWORD` | Least-privilege `pg_monitor`-only role for `postgres-exporter`, created by Flyway `V12` (issue #675) — deliberately **not** `POSTGRES_USER`, so a compromised exporter never gets full DB access |
| `SONAR_DB_USER/PASSWORD`, `SONAR_ADMIN_USER/PASSWORD`, `SONAR_TOKEN` | SonarQube's own database + admin account; `scripts/run-sonar.sh` handles SonarQube's forced first-login password change |

### Runtime enforcement
`ProductionCredentialsGuard` (`@PostConstruct`) checks, only when
`app.environment=production`, whether any of the credential properties still
equal the literal default value `"admin"` — if so, it throws
`IllegalStateException` and the application refuses to start. This is a
fail-closed guard against deploying with `.env.example`'s placeholder values
unchanged.

## Options Considered
- **Docker secrets / Kubernetes Secrets**: Deferred — no Swarm/Kubernetes
  orchestration is in place yet (see ADR-001); revisit if/when a production
  orchestrator is adopted.
- **Cloud secrets manager (AWS Secrets Manager, Vault, etc.)**: Rejected for
  now to keep local/CI setup provider-agnostic and dependency-free,
  consistent with ADR-009's LPG rationale.
- **Per-service `.env` files**: Rejected — `.env` sprawl across
  `docker-compose.yml` and `infra/docker-compose.yml` would risk drift
  (e.g. the exporter's DB password defined in two places); a single file is
  the simplest way to guarantee both stacks agree.

## Consequences
- **Pros**: One file to rotate/audit; `ProductionCredentialsGuard` catches
  the most common deployment mistake (forgetting to change placeholders)
  before the app even starts serving traffic.
- **Cons**: `.env` is still plaintext-on-disk — fine for local/CI, but not a
  substitute for a real secrets manager before an actual production
  deployment (no production deployment target is currently defined, per the
  SAD's Risks section). `ProductionCredentialsGuard` only checks for the
  literal `"admin"` default, not password strength in general.
