# Design

## Context

Docker Hub limits anonymous pulls per source IP; GitHub-hosted runners share IPs, so the limit is hit regardless of this repository's own volume (#1380).

## Goals / Non-Goals

Goal: no PR workflow pulls from Docker Hub anonymously. Non-goals: Docker Hub credentials (no secret exists and fork PRs could not use it), changing production Dockerfiles, CD workflows.

## Decisions

- `mirror.gcr.io` over `docker login` to ghcr: it serves Docker Hub library and community images (`library/postgres`, `testcontainers/ryuk`, `maven`, `eclipse-temurin`) without re-publishing them, needs no token, and is pull-through, so tags stay identical to Docker Hub's. All tags used were checked to resolve (200) on the mirror.
- The pin rule of #1045 still applies to the Docker Hub reference behind the mirror (`security/tests/test_image_pins_and_dependabot.py` strips the prefix).
- oasdiff: the same release and checksum the stale-entry check already used, installed once and shared by both steps; `--format githubactions` keeps PR annotations.

## Riesgos / Trade-offs

If `mirror.gcr.io` is unavailable the jobs fail as they would on a Docker Hub outage. Dependabot does not bump workflow service images in either form.

## Testing Strategy

`workspace/tests/test_ci_docker_hub_mirror.py` (new) asserts the image sources, the oasdiff binary and the browser-install retry; the OpenAPI wiring tests and the image-pin test are adapted. Red first, then green.

## Regression Strategy

workspace, security, contracts and docs verify. The PR's own CI run exercises every changed job (Bruno, Playwright, integration, image build, OpenAPI gate).

## Playwright Strategy

No UI change; the Playwright jobs in CI run unchanged except the install retry.

## Deployment Strategy

Merge only; nothing to deploy.

## Rollback Strategy

Revert the PR; the workflows go back to anonymous Docker Hub pulls and the oasdiff action.
