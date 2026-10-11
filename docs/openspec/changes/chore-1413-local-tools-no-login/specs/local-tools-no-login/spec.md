## Purpose

Local Grafana and SonarQube open without a login for CU77 operators, while
production authentication and non-loopback exposure stay unchanged.

## ADDED Requirements

### Requirement: Local Grafana opens without a login

The local Grafana configuration SHALL enable anonymous authentication with the
Admin organisation role and SHALL disable the login form.

#### Scenario: Grafana anonymous Admin without login form

- **WHEN** an engineer reads `infra/observability/grafana/grafana.ini`
- **THEN** `[auth.anonymous] enabled` is true, the anonymous org role is Admin,
  and `[auth] disable_login_form` is true

### Requirement: Local SonarQube allows anonymous browse

The local SonarQube service SHALL set `forceAuthentication` to false so projects
can be browsed without a login. Analysis and administration MAY still use tokens
or an admin account.

#### Scenario: Sonar forceAuthentication is false

- **WHEN** the observability compose SonarQube service is read
- **THEN** its environment includes `SONAR_FORCEAUTHENTICATION=false`

### Requirement: Grafana and SonarQube are loopback-bound locally

The published host ports for Grafana and SonarQube in the local observability
compose SHALL bind to `127.0.0.1`.

#### Scenario: Ports bind to localhost

- **WHEN** the observability compose Grafana and SonarQube port mappings are read
- **THEN** each mapping starts with `127.0.0.1:`

### Requirement: Production authentication stays required

`docker-compose.prod.yml` and `infra/deploy/kustomize` SHALL not enable Grafana
anonymous auth or Sonar `forceAuthentication=false`, and SHALL not gain the local
loopback Grafana/Sonar port bindings from this change.

#### Scenario: Prod and Kustomize stay authenticated

- **WHEN** production compose and Kustomize overlays are scanned for this change’s
  local-only settings
- **THEN** none of those anonymous / forceAuthentication=false / local bind
  patterns are present there
