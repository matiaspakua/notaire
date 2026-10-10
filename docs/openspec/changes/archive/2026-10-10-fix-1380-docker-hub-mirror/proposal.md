# Pull CI images through mirror.gcr.io (#1380)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1380 |
| Use Case | CU76 – Aseguramiento de Calidad e Infraestructura de Pruebas |
| Branch | `fix/1380_docker_hub_mirror` |
| Gate 1 status | draft |

## Objetivo

Shared GitHub runners hit Docker Hub's anonymous pull rate limit (429 `toomanyrequests`). On Oct 9 it failed Bruno, Playwright and the OpenAPI gate on #1374 twice before any test ran, and left the Testcontainers integration job hanging. The agents' PAT cannot re-run jobs, so each occurrence cost a push. The Owner asked for a durable, simple and safe fix.

## What Changes

- Service containers (`playwright-e2e.yml`, `dast-zap.yml`, `performance-test.yml`): `mirror.gcr.io/library/postgres:16.15-alpine`, Google's public Docker Hub cache (same image, no credentials).
- `ci.yml` integration tests: `TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX=mirror.gcr.io/` (postgres and ryuk).
- `ci.yml` image build: BuildKit mirrors `docker.io` to `mirror.gcr.io`; the Dockerfiles do not change.
- `openapi-contract.yml`: the checksum-verified oasdiff 1.33.0 binary runs the breaking diff (same `--fail-on ERR --err-ignore` rule) instead of the Docker-based `oasdiff-action`.
- `npx playwright install` is retried 3 times (a truncated Chrome download failed a run on Oct 8).

No application, Dockerfile or runtime change.

## Reglas de negocio

None: CI-only change. The OpenAPI gate rule (`--fail-on ERR`, Owner-accepted list) is unchanged.

## Capabilities

- `ci-image-sources` (new): where PR workflows pull container images from.

## Impact Analysis

PR workflows only. Same images and tags, pulled from Google's public cache; oasdiff same version and rule. No application code, Dockerfile, CD or runtime change.

### Módulos afectados

workspace (`.github/workflows`, `workspace/tests`), security (`security/tests/test_image_pins_and_dependabot.py`).

## Documentation Impact

Workflow comments and CHANGELOG. `docs/200-architecture/208-devsecops/README.md` does not name the image source or the oasdiff action, so it is unchanged.
