> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Issue #1044 (audit-2026-09, CU78 + CU75): there is no production application
compose — only a permissive dev stack.

Verified on workspace tip (2026-10-03):

| Location | Finding |
|----------|---------|
| `docker-compose.yml` | Dev stack: postgres `5432`, backend `8080`, frontend `3000`, pgAdmin `5050` published |
| Credential defaults | `${VAR:-admin}` / `admin@notaire.com` throughout postgres, backend, pgAdmin |
| Backend `ENVIRONMENT` | Hard-coded `development` — `ProductionCredentialsGuard` skipped |
| Backend env bloat | Receives `PGADMIN_*`, `GRAFANA_*`, `POSTGRES_EXPORTER_*` |
| Flyway | `SPRING_FLYWAY_BASELINE_ON_MIGRATE: true` |
| pgAdmin | `SERVER_MODE=False`, master password not required, port 5050 |
| `docker-compose.prod.yml` | **missing** |
| Reverse proxy in app stack | **missing** (docs mention TLS terminator conceptually; #254 open) |
| `ProductionCredentialsGuard` | Rejects literal `admin` for datasource/actuator/admin **and** pgAdmin/Grafana/exporter when `app.environment=production` |
| Related | #254 TLS, #256 backups, #901 production target |

Fleet serialize (coordinator): **#1057** (PR #1150) → **#1048** → **#1047** →
then **#1044**. This Gate 1 draft is prep-only; no product branch/PR until
\#1047 is on `main`.

## Goals / Non-Goals

**Goals:**

- Ship `docker-compose.prod.yml` (or documented equivalent) as the production
  app-stack entrypoint.
- No pgAdmin in prod; no host ports on postgres/backend/frontend.
- Reverse proxy is the sole host ingress.
- `ENVIRONMENT=production` + required secrets (`${VAR:?}`) + least-privilege env.
- Flyway baseline-on-migrate disabled in prod.
- Align `ProductionCredentialsGuard` with least-privilege (do not force unused
  service secrets).
- Static tests prove compose invariants; deployment guide updated.

**Non-Goals:**

- Full TLS certbot/ACME productization (#254) beyond a proxy that can terminate
  TLS when certs are provided.
- Automated backups (#256).
- Removing or hardening the **dev** compose beyond leaving it as-is for local use.
- Folding `infra/` observability into the prod app file.
- Implementing before #1047 merges.

## Decisions

1. **New file `docker-compose.prod.yml` (not mutating default compose)**
   - Why: Dev ergonomics (published ports, pgAdmin, baseline-on-migrate) remain
     valuable locally; AC asks for a production artifact / override.
   - Alternative rejected: make root compose “prod-secure by default” — breaks
     current local workflows and `scripts/start.sh` assumptions.

2. **Minimal reverse proxy (nginx or Caddy alpine) in the prod compose**
   - Why: AC requires “no host ports except the reverse proxy”; without a proxy
     service the only compliant option is zero published ports (unusable alone).
   - Alternative rejected: document “bring your own ingress” with zero ports —
     fails the spirit of a runnable production compose.
   - TLS cert lifecycle stays #254; proxy MAY publish `:80` and optionally
     `:443` with operator-mounted certs if trivial; HTTP-only first is acceptable
     if docs state TLS is required in real prod via #254.

3. **Secrets via `${VAR:?message}` with no defaults**
   - Why: Matches #1044 AC and Constitution P9; forces fail-fast at
     `compose up` when `.env` is incomplete.
   - Dev compose retains `${VAR:-admin}` for local speed.

4. **Least-privilege backend env + guard alignment**
   - Why: Issue calls out unrelated Grafana/pgAdmin/exporter secrets on backend.
   - Today `ProductionCredentialsGuard` fails production if those `@Value`
     defaults remain `admin`. Implement MUST either (a) stop checking credentials
     for services not configured/deployed, or (b) stop binding unused properties
     with insecure defaults — prefer (a)/(b) over re-injecting unused secrets.
   - Alternative rejected: keep injecting fake non-admin Grafana passwords into
     backend “to satisfy the guard” — violates least-privilege AC.

5. **Flyway baseline-on-migrate explicitly `false` in prod**
   - Why: CU75 / Flyway SSOT; baseline in prod can mask missing migrations.
   - Dev may keep `true` for empty local volumes (existing behavior).

6. **TDD via static YAML unittest first**
   - Add `scripts/test_prod_compose.py` (pattern:
     `scripts/test_infra_prometheus_hardening.py`) asserting AC invariants
     before/while writing the compose file.
   - Extend `ProductionCredentialsGuardTest` for the unused-credential policy.
   - Prove red with `python3 scripts/test_prod_compose.py` (and Maven unit test)
     before green.

7. **Serialize after #1047**
   - Why: coordinator queue; avoid concurrent heavy CI / product PRs racing the
     same runners while #1057/#1048/#1047 land.

## Riesgos / Trade-offs

- **[Risk] ProductionCredentialsGuard vs least-privilege** → Design decision 4;
  implement must keep rejecting `admin` for in-scope secrets while not requiring
  absent services’ passwords.
- **[Risk] Operators expect published `:8080`/`:5432` from docs/muscle memory** →
  Deployment guide must be explicit; keep dev compose for local.
- **[Risk] Reverse proxy misroutes API vs frontend** → Keep routing table tiny
  (`/` → frontend, `/api` + `/actuator` → backend); cover with compose config
  asserts and a Gate 5 smoke through the proxy.
- **[Risk] `${VAR:?}` breaks casual `docker compose -f ... up` without `.env`** →
  Intended; document required keys in deployment guide + `.env.example` pointer.
- **[Risk] HTTP-only proxy before #254** → Document that real production MUST
  terminate TLS; do not claim #254 closed.
- **[Trade-off] Separate prod file vs override** → Separate file is clearer for
  audit “production artifact exists”; override-only is acceptable only if
  README names it as the production entrypoint and tests target that file.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Prod compose exists; no pgAdmin | unit | `scripts/test_prod_compose.py` |
| No host ports on postgres/backend/frontend | unit | same |
| Only reverse proxy publishes ports | unit | same |
| `ENVIRONMENT=production` | unit | same |
| `${VAR:?}` secrets / no `:-admin` | unit | same |
| Least-privilege backend env | unit | same |
| Flyway baseline-on-migrate off | unit | same |
| Guard does not require unused-service defaults | unit | `ProductionCredentialsGuardTest` |
| Deployment guide documents prod path | review | PR checklist / optional doc assert |
| Compose config validity (optional) | command | `docker compose -f docker-compose.prod.yml config` with dummy env |

- New unit tests: stdlib unittest for compose YAML + JUnit for guard.
- New integration tests: optional compose-up smoke in implement env if Docker
  available; not required for Gate 2 if static asserts cover AC.
- Coverage impact (JaCoCo): small if guard changes; keep ratchet green.

## Regression Strategy

- Existing tests affected: `ProductionCredentialsGuardTest` (expectations for
  unused credentials); ensure development-mode behavior unchanged.
- Full suite: `python3 scripts/test_prod_compose.py`;
  `mvn test -pl backend-api -Dtest=ProductionCredentialsGuardTest`;
  `bash scripts/preflight.sh` as applicable; heavy CI
  `bash scripts/check-heavy-ci.sh <pr>`.
- Dev `docker-compose.yml` MUST remain usable (`scripts/start.sh` path).
- HTTP/Bruno: n/a for product API delta.

## Playwright Strategy

- No UI product change. No new Playwright scenarios.
- PR still must pass repository heavy CI (includes Playwright) before merge.
- Mark product E2E n/a for this capability; do not skip the PR Playwright job.
- Gate 5 smoke is compose/proxy HTTP health, not Playwright.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: add prod compose (+ proxy config) → update docs →
  operators use `docker compose -f docker-compose.prod.yml --env-file .env up -d`
  (exact command in deployment guide)
- Configuration or `.env` keys: document required production secrets (no new
  committed secrets); optional `.env.example` section
- Feature flag: no
- Smoke test after deploy (Gate 5): with a throwaway `.env` of non-`admin`
  secrets, `compose config` succeeds; stack becomes healthy; curl via reverse
  proxy reaches frontend and `/api` or actuator health; confirm postgres port
  not listening on host

## Rollback Strategy

- Revert safe: yes — delete/revert prod compose leaves only the existing (known
  insecure-for-prod) dev stack; no schema change
- Database rollback: none needed
- Data written under the new behavior after revert: none from compose file alone
- Blast radius if rollback delayed: low for current prod (no prod compose in use
  yet); medium if operators already cut over — keep previous compose revision

## Migration Plan

1. Wait for **#1047** merge to `main` (after #1057 and #1048).
2. Copy this draft into `openspec/changes/feat-1044-prod-compose/`.
3. Validate with `bash scripts/validate-sdlc-plan.sh feat-1044-prod-compose`.
4. Branch `cursor/feat-1044-prod-compose-69d3` → failing compose/guard tests →
   implement prod compose + guard alignment → green tests → docs → PR
   `Closes #1044`.
5. After merge: Gate 5 compose smoke with non-default secrets.

## Open Questions

- Prefer nginx vs Caddy for the in-compose proxy — choose the smaller pinned
  image already familiar to the team at implement time; not AC-blocking.
- Whether to publish only `:80` initially and document `:443` under #254 — yes
  unless adding a trivial cert-mount path is free.
- Whether `docker-compose.cloud.yml` needs a prod sibling — out of scope unless
  cloud agents must validate prod compose; keep cloud override for nested-Docker
  **dev** networking.
