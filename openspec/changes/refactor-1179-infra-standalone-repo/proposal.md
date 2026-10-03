# Prepare infra/ as a standalone repository (staged in the monorepo)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1179 (related: #302 IaC documentation) |
| Use Case | CU77 – Operations Monitoring and Incident Management (also CU75, CU78 via deploy assets) |
| Branch | `refactor/1179_infra_standalone_repo` |
| Gate 1 status | draft — awaiting Owner approval |

## Objetivo

Infrastructure assets are spread over `infra/`, `deploy/`, `performance-test/`
and the root, and `infra/` itself reaches back into the repo root (`.env`,
`REPO_DIR`, app network). This change gathers every infrastructure asset into
one self-contained `infra/` folder, documented on its own, so it can later be
split into a separate repository (part of the Notaire system) with no further
restructuring. Project-level documentation stays in `docs/`; the specifics of
how to configure, prepare, define and run the infra live in `infra/`.

## What Changes

- Reorganize `infra/` into `observability/`, `deploy/`, `performance/`,
  `scripts/`, `docs/` (via `git mv`, history preserved).
- Move `deploy/kustomize` and `deploy/nginx` to `infra/deploy/`, and
  `performance-test/k6` to `infra/performance/k6`.
- Delete the stale `infra/tests/e2e/` suite (superseded by `frontend/tests/e2e/`,
  unreferenced, last touched in #383).
- Make `infra/` self-contained: `infra/.env.example`, scripts resolve paths
  relative to `infra/` and accept an env file, no paths escaping the folder.
- Write `infra/README.md` plus `infra/docs/{PREPARATION,CONFIGURATION,DEFINITION,OPERATION}.md`.
- Repoint every consumer: workflows (`performance-test.yml`, `codeql.yml`),
  `scripts/start-all.sh`, `docker-compose.prod.yml` nginx mount, `.gitignore`,
  `.env.example`, agent rule files, guard tests under `scripts/`.
- Add a guard test asserting the layout and self-containment.
- Update permanent docs to link to `infra/` instead of duplicating how-to.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| All infrastructure assets MUST live under `infra/`; the folder MUST NOT reference paths outside itself, except the documented application coupling | #1179; CONSTITUTION §8 (no duplication) | New |
| Secrets MUST stay out of git; infra variables are documented in `infra/.env.example` | Constitution P9; ADR-019 | Made explicit |
| Compose files that build or override the application (`docker-compose.yml`, `.cloud.yml`, `.prod.yml`) stay at the root; they are the application composition, not infra | #1179 assessment | New |
| `docs/` holds project-level docs; infra specifics live in `infra/` and are linked, never copied | #1179; Constitution §8 | New |
| Local gates MUST mirror CI after the move | CLAUDE.md CI Preflight | Made explicit |

## Capabilities

### New Capabilities

- `infra-standalone-repo`: layout, self-containment and documentation contract
  for `infra/` as a repository-ready folder.

### Modified Capabilities

- (none under `openspec/specs/`; `prod-docker-compose` and
  `staging-deploy-manifests` keep their requirements, only file paths move)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | No Java change; Flyway `V12` only mentions the exporter role |
| `frontend` | no | — |
| `frontend-swing` | no | Removed module, out of scope |
| `notaire-shared` | no | — |
| `infra/` | yes | Reorganized, documented, made self-contained |
| `deploy/`, `performance-test/` | yes | Moved into `infra/`, directories removed |
| CI/CD (`.github/workflows`) | yes | Path updates only (k6 file, CodeQL paths-ignore) |
| Scripts / tests | yes | `start-all.sh`, `start-infra.sh`, guard tests repointed; new guard test |

### Surface area

- Entities / Endpoints / Flyway: none
- Configuration / `.env`: new `infra/.env.example`; root `.env.example` keeps a pointer
- Dependencies: none

### Architecture review

Pure relocation and documentation; no behaviour change and no new platform
service. The remaining app→infra coupling (prod compose mounts the nginx
config; observability attaches to the app network and scrapes the backend) is
documented explicitly, because the later repository split must replace it with
published images (GHCR, already produced by `cd.yml`). No new ADR; ADR-016/017
text is updated for paths only.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `README.md`, `CLAUDE.md`, `AGENTS.md` | Replace `infra/...`, `deploy/...`, `performance-test/...` paths; point to `infra/README.md` |
| `docs/200-architecture/201-SAD/sad.md` | Update path references; link to `infra/README.md` |
| `docs/200-architecture/202-ADR/ADR-009, 016, 017, 019` | Path updates only |
| `docs/200-architecture/207-monitoring/README.md` | Keep the architecture view; delegate run/config details to `infra/docs/` |
| `docs/200-architecture/209-deployment/README.md`, `docs/300-development/DEPLOYMENT-PLAN.md` | New kustomize/nginx paths; link to `infra/docs/` |
| `docs/200-architecture/204-diagrams/deployment-docker.puml` | Path labels |
| `docs/100-business/102-use-cases/CU77 – Operations Monitoring and Incident Management.md` | Add #1179 to the GitHub ID table |
| `CHANGELOG.md` | `[Unreleased]` entry |
| `infra/README.md`, `infra/docs/*` (new, engineering how-to) | Authoritative infra specifics |

## Out of Scope

- Creating the new GitHub repository and splitting history (follow-up issue).
- Moving the root compose files or Dockerfiles (application composition).
- Moving `.github/workflows/` (GitHub requires the repo root).
- Switching prod compose from source builds to GHCR images.
- Any change to observability behaviour, dashboards, alerts or credentials.
- The IaC/Terraform/cost content of #302 (this change only prepares the home for it).
