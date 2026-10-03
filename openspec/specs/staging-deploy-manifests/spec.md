# staging-deploy-manifests Specification

## Purpose
Provide a KIS staging/production Kubernetes deploy target (Kustomize) that
mirrors the #1044 four-service production compose topology, with static
validation and honest deployment documentation. Source: #901; owner CU77.
## Requirements
### Requirement: Kustomize base renders the #1044 service set

The repository MUST include a Kustomize base (or equivalently documented
manifest set under `deploy/kustomize/` or `k8s/`) that renders Kubernetes
resources for postgres, backend, frontend, and an ingress/proxy service — the
same four logical services as `docker-compose.prod.yml`. It MUST NOT introduce
unrelated platform services (operators, mesh, observability stack) as part of
this base.

#### Scenario: Base manifests exist for the four app services

- **WHEN** an operator or validator builds the Kustomize base (or loads the
  documented manifest set)
- **THEN** the rendered output includes Deployments (or equivalent) and
  Services for postgres, backend, frontend, and an ingress/proxy

#### Scenario: Staging overlay stays within the same service set

- **WHEN** the staging overlay is built
- **THEN** it does not add application services beyond the #1044 four-service
  set (image/tag/config overlays only)

### Requirement: Data plane isolation parity with production compose

PostgreSQL MUST NOT be exposed via a public host NodePort/LoadBalancer that
re-opens the database on the public host network. Ingress to the application
MUST go through the proxy/ingress resource.

#### Scenario: Postgres is not publicly published

- **WHEN** the rendered staging/prod manifests are inspected for Service types
  and ports for postgres
- **THEN** postgres is ClusterIP (or equivalent internal-only) and is not
  published as a public host NodePort/LoadBalancer for the database port

### Requirement: Secrets are placeholders only

Manifests MUST reference Kubernetes Secrets (or documented external secret
refs) for credentials. The repository MUST NOT commit real production
passwords, JWT secrets, or actuator credentials.

#### Scenario: No committed credential values

- **WHEN** a reviewer inspects the new manifest / overlay files in git
- **THEN** credential fields use Secret references or explicit placeholders
  and do not contain real passwords or JWT material

### Requirement: Production environment and Flyway posture in manifests

The backend container environment in the manifests MUST set
`ENVIRONMENT=production` (or a documented staging-equivalent that activates
the same production credential guard posture) and MUST NOT enable Flyway
baseline-on-migrate.

#### Scenario: Backend ENVIRONMENT is production (or documented equivalent)

- **WHEN** the backend container env in the rendered manifests is inspected
- **THEN** `ENVIRONMENT` is `production` or the documented staging-equivalent
  value that activates production security posture

#### Scenario: Flyway baseline-on-migrate disabled in manifests

- **WHEN** the backend container env in the rendered manifests is inspected
- **THEN** `SPRING_FLYWAY_BASELINE_ON_MIGRATE` is absent or explicitly `false`
  (never `true`)

### Requirement: Static validation proves the deploy target

A static validator (script and/or CI job) MUST fail if the required manifests
are missing or diverge from the four-service set / isolation invariants.

#### Scenario: Static validator fails when manifests are incomplete

- **WHEN** the required base/overlay files are removed or the four-service set
  is incomplete
- **THEN** the static validator exits non-zero

#### Scenario: Static validator passes on the shipped manifests

- **WHEN** the validator runs against the committed Kustomize base + staging
  overlay
- **THEN** it exits zero

### Requirement: Deployment docs describe the apply path honestly

Permanent deployment documentation MUST describe how to apply the manifests,
how they relate to `docker-compose.prod.yml`, and that CD image publish alone
does not imply automated cluster deploy unless a real deploy job exists.

#### Scenario: Deployment guide documents manifests and compose relationship

- **WHEN** a reviewer reads `docs/200-architecture/209-deployment/README.md`
  (and `DEPLOYMENT-PLAN.md` as needed) after the change
- **THEN** the docs name the Kustomize path, required Secret keys, apply
  steps, relationship to prod compose, and do not claim automated production
  cluster deploy unless such a job actually exists

