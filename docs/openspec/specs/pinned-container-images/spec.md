# pinned-container-images Specification

## Purpose

Keep local, prod, and infra container image references reproducible by
forbidding floating tags and requiring minor-version or digest pins for every
image named in #1045. Source: #1045; CU78.

## Requirements

### Requirement: Compose and infra images are pinned

Application and observability compose files MUST reference container images
only by a **minor version tag** (for example `16.6`, `8.14`, `10.6-community`)
or by **digest** (`image@sha256:…`). The tags `:latest`, unversioned channel
tags such as `sonarqube:community`, and major-only floats such as `postgres:16`
or `postgres:15` MUST NOT appear as the sole version selector for images in
scope.

#### Scenario: pgadmin is not on latest

- **WHEN** `docker-compose.yml` is inspected for the pgAdmin service image
- **THEN** the image is not `dpage/pgadmin4:latest` and is pinned to a minor
  version or digest

#### Scenario: infra stack has no latest tags

- **WHEN** `infra/docker-compose.yml` is inspected for `image:` lines
- **THEN** none of those images use the `:latest` tag and each is pinned to a
  minor version or digest

#### Scenario: sonarqube channel tag is versioned

- **WHEN** the SonarQube service image in `infra/docker-compose.yml` is
  inspected
- **THEN** it is not the bare `sonarqube:community` channel tag and includes an
  explicit minor (or digest) pin within the community edition family

#### Scenario: postgres images are minor-or-digest pinned

- **WHEN** postgres service images in `docker-compose.yml`,
  `docker-compose.prod.yml`, and `infra/docker-compose.yml` are inspected
- **THEN** each is pinned to a postgres minor version or digest (not
  major-only `postgres:16` / `postgres:15` alone)

### Requirement: Dockerfile bases are pinned

Application Dockerfiles MUST pin every `FROM` base image to a minor version or
digest within the Alpine / Temurin / Node families already chosen for the
project.

#### Scenario: backend Dockerfile bases are pinned

- **WHEN** `backend-api/Dockerfile` and `backend-api/Dockerfile.slim` are
  inspected
- **THEN** `maven` and `eclipse-temurin` `FROM` lines are pinned to a minor
  version or digest (not floating `maven:3.9-…` / `eclipse-temurin:21-jre-alpine`
  major-channel tags alone)

#### Scenario: frontend Dockerfile bases are pinned

- **WHEN** `frontend/Dockerfile` is inspected
- **THEN** every `node` `FROM` line is pinned to a minor version or digest
  (not floating `node:22-alpine` alone)

#### Scenario: CI postgres service images follow the same pin rule

- **WHEN** GitHub Actions workflows that start a postgres service container are
  inspected
- **THEN** those postgres images are pinned to a minor version or digest
  consistent with the compose postgres pin policy
