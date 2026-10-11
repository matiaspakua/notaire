## Context

#1408 delivered overview dashboards. Operators still hit Grafana and SonarQube
login forms on the local stack. Issue #1413 asks for local-only anonymous access
with loopback port binding so removing auth does not expose the tools on the LAN.
Production compose and Kustomize must keep credentials.

## Goals / Non-Goals

**Goals:**

- Grafana: anonymous enabled, Admin role, login form disabled.
- SonarQube: `forceAuthentication=false` for anonymous browse.
- Bind Grafana and SonarQube published ports to `127.0.0.1`.
- Guard tests cover the above and assert prod/Kustomize unchanged.
- Document in ADR-016 and infra ops/config docs.

**Non-Goals:**

- Anonymous admin for SonarQube (not supported; analysis stays token-based).
- Changing Prometheus/Loki/Homer (already open).
- CHANGELOG while #1518 holds the lock.

## Decisions

1. **Configure Grafana in `grafana.ini`, not only env vars.** The file is already
   the mounted source of truth for auth. Set `[auth.anonymous] enabled = true`,
   `org_role = Admin`, and `[auth] disable_login_form = true`.
   - Alternative: `GF_AUTH_*` env only — rejected (duplicates the ini and drifts).

2. **Sonar via `SONAR_FORCEAUTHENTICATION=false`.** Official Docker env mapping for
   `sonar.forceAuthentication`. Analysis scripts keep using tokens/admin for writes.
   - Alternative: custom `sonar.properties` volume — rejected (env is enough).

3. **Loopback bind on the two published ports.** `"127.0.0.1:3001:3000"` and
   `"127.0.0.1:9000:9000"`. Other infra ports unchanged in this slice.
   - Alternative: leave `0.0.0.0` — rejected (AC requires localhost bind).

4. **Prod/Kustomize assertion by absence.** Guards confirm those trees still have
   no Grafana/Sonar anonymous settings and do not publish those local binds.

## Riesgos / Trade-offs

- [Anonymous Admin on Grafana] → Loopback-only publication; local-dev threat model.
- [Existing Grafana sessions/volumes] → Operators may need one container recreate
  after pull; documented in OPERATION.
- [Sonar anonymous cannot administer] → Intentional; AC only requires browsing.

## Testing Strategy

| Scenario | Level | Test |
|----------|-------|------|
| Grafana anonymous Admin, no login form | unit | `infra/tests/test_local_tools_no_login.py` |
| Sonar forceAuthentication false | unit | same |
| Ports bound to 127.0.0.1 | unit | same |
| Prod and Kustomize unchanged | unit | same |

Red first before editing ini/compose.

## Regression Strategy

`bash infra/verify.sh` including Homer and dashboard guards. No backend/frontend.

## Playwright Strategy

n/a — no product UI. Infra path + docs; Playwright stays path-filtered off while
#1518 occupies heavy runners.

## Deployment Strategy

Recreate Grafana and SonarQube containers (`start-infra.sh` or compose up). No
Flyway. Local URLs stay `http://localhost:3001` / `:9000`.

## Rollback Strategy

Revert the PR; restore auth and prior port bindings. Grafana volume may retain
anonymous preference until recreate — acceptable for local.
