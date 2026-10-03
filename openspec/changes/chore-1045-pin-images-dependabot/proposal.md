# Pin container images and enable Dependabot npm + docker

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1045 |
| Use Case | **CU78** – Security, Privacy and Compliance |
| Branch | `cursor/chore-1045-pin-images-dependabot-69d3` |
| Gate 1 status | draft ready (internal); implement **after #1043** (queue: #1046 → #1042 → #1041 → #1040 → #1043 → #1045) or as coordinator schedules |

## Objetivo

Floating container tags (`:latest`, major-only, channel tags such as
`sonarqube:community`) and incomplete Dependabot coverage leave local/prod
compose and Dockerfile bases non-reproducible and without automated update
PRs for Docker (and risk gap if npm were removed). Pin every named image to a
**minor version or digest**, and ensure Dependabot covers **npm** (`frontend/`)
and **docker** ecosystems so supply-chain updates stay continuous.

## What Changes

- Pin all unpinned / floating image references to a **minor tag** (preferred)
  or **digest** (`@sha256:…`) in:
  - `docker-compose.yml` (`postgres:16`, `dpage/pgadmin4:latest`)
  - `docker-compose.prod.yml` (`postgres:16`; tighten `nginx:1.27-alpine` if
    still floating beyond minor)
  - `infra/docker-compose.yml` (`b4bz/homer:latest`, `sonarqube:community`,
    `postgres:15`, `prom/prometheus:latest`,
    `prometheuscommunity/postgres-exporter:latest`, `grafana/grafana:latest`,
    `grafana/loki:latest`, `grafana/promtail:latest`)
  - `backend-api/Dockerfile` / `Dockerfile.slim` (`maven:3.9-…`,
    `eclipse-temurin:21-jre-alpine`)
  - `frontend/Dockerfile` (`node:22-alpine`)
  - CI service containers that float the same families
    (e.g. `postgres:16-alpine` in Playwright / performance workflows) for
    consistency with the postgres pin
- Extend `.github/dependabot.yml`:
  - **npm** under `/frontend` — **verify present** (already on `origin/main`
    since #1136); add only if missing; never duplicate
  - **docker** — add entries for directories that own Dockerfiles
    (`/backend-api`, `/frontend`; add further directories only if a Dockerfile
    lives there)
- Document the pin policy and Dependabot ecosystems in DevSecOps / infra docs
  + CHANGELOG
- **No product API/UI behavior change.** No Flyway. No Java/TS feature work.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Application, infra, and CI container images MUST be pinned to a minor version or digest (no `:latest` / floating major-only / unversioned channel tags) | CU78; #1045 AC | New (made explicit) |
| Dependabot MUST update npm dependencies under `frontend/` | CU78; #1045 AC; gh-secure baseline | Made explicit (idempotent: keep if present) |
| Dependabot MUST update Docker base images for application Dockerfiles | CU78; #1045 AC | New |
| Pin choices MUST preserve Alpine / Temurin / Node major families already chosen in ADR-017 | ADR-017 | Unchanged (constrain pin within family) |

## Capabilities

### New Capabilities

- `pinned-container-images`: Repository compose files, Dockerfiles, and related
  CI service images use only minor-version or digest pins for the images named
  in #1045 AC.
- `dependabot-npm-docker`: `.github/dependabot.yml` includes npm (`/frontend`)
  and docker ecosystems (Dockerfile directories) alongside existing maven and
  github-actions entries.

### Modified Capabilities

- (none under `openspec/specs/` today cover image pinning or Dependabot
  ecosystem coverage)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes (Dockerfile only) | Pin `FROM` bases in `Dockerfile` / `Dockerfile.slim` |
| `frontend` | yes (Dockerfile only) | Pin `FROM` bases in `Dockerfile` |
| `frontend-swing` | no | — |
| `notaire-shared` | no | — |
| `infra` / observability | yes | Pin all `image:` tags in `infra/docker-compose.yml` |
| CI/CD (`.github/workflows`) | yes | Pin floating CI service images (postgres family); Dependabot config |
| Root compose | yes | `docker-compose.yml`, `docker-compose.prod.yml` image pins |
| Scripts / docs | yes | Hygiene tests + DevSecOps/infra/ADR-017 notes + CHANGELOG |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (image tags only; credentials unchanged)
- Dependencies: container image tag pins; Dependabot ecosystems
- **BREAKING**: operators who relied on floating `:latest` must pull the pinned
  tags after merge (intentional reproducibility)

### Architecture review

Follows ADR-017 (Alpine multi-stage backend/frontend bases) — pins **within**
existing families rather than switching distros. No new ADR required; update
ADR-017 Consequences / Status to record the pin + Dependabot docker policy.
Compose/infra remain the local observability stack; no architecture change to
microservices boundaries.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-017-container-base-images.md` | Record pin-to-minor-or-digest policy; note Dependabot docker updates Dockerfile bases |
| `docs/200-architecture/208-devsecops/README.md` | Dependabot table: add **docker**; keep npm/maven/actions accurate |
| `infra/README.md` | Note pinned image tags / where pins live; how to bump via Dependabot |
| `docs/100-business/102-use-cases/CU78 – Security and Compliance.md` | Brief supply-chain / image-pin hygiene note if CU tracks ops AC |
| `CHANGELOG.md` | `[Unreleased]` entry: image pins + Dependabot docker (npm verified) |

## Out of Scope

- **#1046** — Dependabot *alert* remediation (`smol-toml` override + delete
  `deprecated-frontend-swing` / log4j). Different scope; **serialize before
  this change** in the fleet queue so Swing delete and frontend lockfile work
  land first. Do not absorb alert fixes here.
- **#1043** — Frontend GHCR publish + semver releases (pins Dockerfile bases
  that #1043 will build; implement **after** #1043 unless coordinator
  overrides).
- Switching base OS families (Debian/distroless) or major Node/Java bumps.
- Renovate, Snyk, or non-Dependabot updaters.
- Prod compose GHCR pull / image promotion (owned by other audit issues).
- Product UI/auth/CSP (#1051) or main ruleset (#1040) work.
