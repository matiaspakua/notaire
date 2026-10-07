# Staging/production deploy manifests (Kustomize overlay of #1044)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #901 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `cursor/feat-901-staging-deploy-manifests-69d3` |
| Gate 1 status | validated; implement in progress (queue ahead cleared; #1067 merged) |

## Objetivo

SAD §11.1 risk “no production deployment target” remains only partially
mitigated: #1044 shipped `docker-compose.prod.yml` + reverse-proxy ingress, but
the repo still has no `k8s/` / Kustomize / Helm manifests and CD only publishes
GHCR images without a documented apply path for a staging topology. This change
adds a **KIS** staging/prod manifest set that mirrors the #1044 four-service
stack so operators have a reproducible non-compose deploy target.

## What Changes

- Add a minimal **Kustomize** base (preferred) under `deploy/kustomize/` or
  `k8s/` defining Deployments/Services for postgres, backend, frontend, and
  ingress/proxy — the same service set as `docker-compose.prod.yml`.
- Add a **staging** overlay (image tags from GHCR for backend; frontend
  documented as build-from-source or GHCR once #1043 lands).
- Secret placeholders via Kubernetes `Secret` / external refs — **no**
  committed credentials; parity with compose `${VAR:?}` posture.
- Static validator (kustomize build / kubeconform / `scripts/test_*.py`)
  proving the required service set and isolation invariants.
- Optional CI job that **validates** manifests (not a live cluster deploy unless
  a real staging host already exists).
- Update deployment docs, `DEPLOYMENT-PLAN.md` §1 (acknowledge prod compose +
  new manifests), CU77 GitHub ID table (#901), and CHANGELOG.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A reproducible non-dev deploy topology MUST exist for staging/prod apply | CU77; SAD §11.1 / §11.3; #901 AC | Made explicit (manifests) |
| Staging/prod manifests MUST mirror the #1044 service set (no invented platform services) | #901 KIS; #1044 prod compose | New |
| Production DB MUST NOT be published on the public host network | CU78 isolation inherited from #1044; #901 | Made explicit (K8s Services) |
| Secrets MUST be operator-supplied placeholders — never committed | CU78; Constitution P9; #901 | Made explicit |
| Backend MUST run with production (or documented staging-equivalent) environment and Flyway baseline-on-migrate disabled | CU75 / #1044 posture | Made explicit for manifests |
| CD MUST either validate manifests in CI or document “publish GHCR + manual apply” without claiming automated prod deploy | #901 AC (pipeline); current `cd.yml` publish-only | Made explicit |

## Capabilities

### New Capabilities

- `staging-deploy-manifests`: Minimal Kustomize (or equivalent) base + staging
  overlay for the #1044 app topology, with static validation and deployment
  documentation — not a full cluster platform.

### Modified Capabilities

- (none under `openspec/specs/` today; `prod-docker-compose` from #1044 remains
  the compose path and is not requirement-changed here)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no product code | Consumed as GHCR image; no Java change expected |
| `frontend` | no product code | Image/build documented; may depend on #1043 for GHCR frontend |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `deploy/` or `k8s/` | yes | New Kustomize base + staging overlay + proxy/ingress config |
| `infra` observability | no | Remains separate from app manifests |
| CI/CD (`.github/workflows`) | maybe | Optional manifest-validate job; no fake “deployed to prod” job |
| Scripts / tests | yes | New static validator analogous to `scripts/test_prod_compose.py` |

### Surface area

- Entities: none
- Endpoints: none (ingress routes existing app; no API contract change)
- Database (Flyway `V{n}`): none (behavior: baseline-on-migrate off in manifests)
- Configuration / `.env`: document required Secret keys; no committed secrets
- Dependencies: optional `kustomize` / `kubeconform` in CI or docs for local validate

### Architecture review

Extends the #1044 **production compose** topology into Kubernetes manifests via
Kustomize. Does **not** invent operators, service mesh, multi-cluster, or GitOps
platforms. TLS (#254), backups (#256), SLOs (#306), and runbooks (#288) stay on
their own issues. No new ADR required if the approach is “Kustomize overlay of
existing compose services”; cite SAD §11.3 and deployment docs.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/209-deployment/README.md` | Document Kustomize path, apply steps, relationship to `docker-compose.prod.yml`, secrets posture |
| `docs/300-development/DEPLOYMENT-PLAN.md` | Correct §1 “no staging/prod target” drift; distinguish compose vs manifests vs automated env deploy |
| `docs/100-business/102-use-cases/CU77 – Operations Monitoring and Incident Management.md` | Add #901 to GitHub ID table; note staging manifest target |
| SAD §11.1 risk text (if still claims “no manifests”) | Point to shipped compose + new manifests without claiming full CD-to-cluster |
| `CHANGELOG.md` | `[Unreleased]` devops entry for #901 |

## Out of Scope

- Full Helm chart ecosystem, operators, service mesh, multi-cluster, GitOps
  control plane.
- TLS/ACME productization (#254).
- Automated PostgreSQL backup/DR (#256).
- SLO/SLI dashboards (#306).
- Incident runbook suite (#288).
- Merging `infra/` observability into app cluster manifests.
- Inventing a cloud vendor account or live staging cluster inside the repo.
- Replacing or rewriting `docker-compose.prod.yml` (#1044 already shipped).
