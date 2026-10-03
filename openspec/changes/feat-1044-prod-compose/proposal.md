# Production docker-compose (no pgAdmin, no exposed DB)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1044 |
| Use Case | **CU78** – Security, Privacy and Compliance; **CU75** – Database Management and Migrations |
| Branch | `cursor/feat-1044-prod-compose-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement **after #1047 merges** (queue: #1057 → #1048 → #1047 → #1044) |

## Objetivo

The only application compose file today (`docker-compose.yml`) is a **dev**
stack: Postgres `5432`, backend `8080`, frontend `3000`, and pgAdmin `5050` are
published on `0.0.0.0`; credentials fall back to `admin`; pgAdmin runs with
`SERVER_MODE=False` (no login); `ENVIRONMENT: development` is hard-coded so
`ProductionCredentialsGuard` never activates; the backend receives unrelated
Grafana/pgAdmin/exporter secrets; and `SPRING_FLYWAY_BASELINE_ON_MIGRATE: true`.
There is no production deployment artifact for the app stack (audit-2026-09
finding #1044). Add a production compose (or override) that removes pgAdmin,
keeps the DB (and app services) off the host network except via a reverse
proxy, requires secrets with `${VAR:?}`, sets `ENVIRONMENT=production`,
disables Flyway baseline-on-migrate, and documents the deploy path.

## What Changes

- Add `docker-compose.prod.yml` (preferred) — or a clearly documented prod
  override — containing postgres + backend + frontend + a reverse-proxy entry
  service; **no pgAdmin**.
- Publish **no host ports** for postgres/backend/frontend; only the reverse
  proxy publishes host ports (HTTP and/or HTTPS listener).
- Set `ENVIRONMENT=production` so `ProductionCredentialsGuard` and production
  security posture activate.
- Require secrets via Compose `${VAR:?message}` (no `admin` / empty defaults)
  for every credential the prod stack needs; least-privilege env per service
  (backend does not receive Grafana/pgAdmin/exporter secrets).
- Set Flyway baseline-on-migrate **disabled** in prod (`false` or omitted).
- Narrow `ProductionCredentialsGuard` (or equivalent wiring) so production
  startup does not require credentials for services **not** deployed by the
  prod compose, while still rejecting default `admin` for credentials that
  remain in scope (datasource, JWT, actuator, app admin).
- Add a static validator test (stdlib unittest parsing compose YAML) proving
  the AC invariants (TDD).
- Document the production compose path in the deployment guide + CU78/CU75 +
  CHANGELOG; keep root `docker-compose.yml` as the **dev** stack.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Production DB MUST NOT be reachable from the public host network | CU78 isolation; RF #85; #1044 AC | Made explicit (compose ports) |
| Production stack MUST NOT include unauthenticated admin UIs (pgAdmin) | CU78; #1044 AC | New for prod artifact |
| Production secrets MUST be operator-supplied (no insecure defaults) | CU78; ADR-019; `ProductionCredentialsGuard`; #1044 AC | Made explicit (`${VAR:?}`) |
| Flyway is SSOT; baseline-on-migrate MUST NOT be enabled in production | CU75; Flyway rules; #1044 AC | Changed for prod (dev may keep baseline for empty local DBs) |
| Only reverse-proxy ports are published on the host in production | #1044 AC; CU78 TLS edge (#254 related) | New |

## Capabilities

### New Capabilities

- `prod-docker-compose`: Production application compose artifact with
  least-privilege secrets, no pgAdmin, DB/app services internal-only, reverse
  proxy as sole host ingress, Flyway baseline-on-migrate off, and deployment
  documentation.

### Modified Capabilities

- (none under `openspec/specs/` today cover a production compose file)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (minimal) | Possibly `ProductionCredentialsGuard` (+ unit tests) so unused service credentials are not required when those services are absent from prod compose |
| `frontend` | no product code | Image still built/used by compose; no Next.js source change expected |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| Root compose / proxy config | yes | New `docker-compose.prod.yml` (+ minimal nginx/Caddy config as needed) |
| `infra` observability compose | no (explicit non-goal) | Remains separate; prod app compose does not start pgAdmin/Grafana |
| CI/CD | maybe | Optional smoke or compose-config job; not required if static unittest covers AC |
| Scripts / tests | yes | New `scripts/test_prod_compose*.py` (or similar) asserting prod invariants |

### Surface area

- Entities: none
- Endpoints: none (routing via reverse proxy only — no API contract change)
- Database (Flyway `V{n}`): none (behavior: baseline-on-migrate off in prod)
- Configuration / `.env`: document required prod keys; `.env.example` may gain
  a short production section or pointer (no hardcoded secrets)
- Dependencies: reverse-proxy image pin (nginx or Caddy alpine) in prod compose

### Architecture review

Adds a **production deployment artifact** alongside the existing **dev**
compose. Does not replace local `scripts/start.sh` / `docker-compose.yml`.
TLS certificate lifecycle remains primarily #254; this change provides the
ingress service and host-port discipline. Backups remain #256. Production
target epic context: #901. Serialize implement after **#1047** per fleet
queue (`#1057 → #1048 → #1047 → #1044`).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/209-deployment/README.md` | Document prod compose file, required secrets, no-pgAdmin, internal DB, reverse-proxy-only ports, Flyway baseline off |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Note prod compose isolation / required secrets posture for #1044 |
| `docs/100-business/102-use-cases/CU75 – Database Management and Migrations.md` | Note prod Flyway baseline-on-migrate disabled |
| `.env.example` | Clarify prod-required vars / pointer (no default secrets for prod) |
| `CHANGELOG.md` | `[Unreleased]` devops/security entry for #1044 |
| Optional: ADR-019 or deployment diagram if prose becomes stale | Point to prod compose without duplicating secrets policy |

## Out of Scope

- Implementing TLS certificate issuance/renewal end-to-end (#254).
- Automated PostgreSQL backups (#256).
- Changing or removing the **dev** `docker-compose.yml` / pgAdmin for local use.
- Merging observability (`infra/docker-compose.yml`) into the prod app compose.
- Full Kubernetes / cloud IaC rewrite.
- Frontend image release/semver (#1043) beyond what prod compose needs to build/run.
- Starting implement / PR before **#1047** merges.
