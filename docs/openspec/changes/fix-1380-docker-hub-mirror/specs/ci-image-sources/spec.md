# ci-image-sources — delta

## Purpose

PR CI does not fail on Docker Hub's anonymous pull rate limit.

## ADDED Requirements

### Requirement: PR workflows pull images through mirror.gcr.io

Service containers, Testcontainers and BuildKit image builds in PR workflows SHALL pull Docker Hub images through `mirror.gcr.io`, the OpenAPI gate SHALL run oasdiff from its checksum-verified release binary, and the Playwright browser install SHALL be retried.

#### Scenario: Service container

- **WHEN** a workflow declares a service container
- **THEN** its image starts with `mirror.gcr.io/`

#### Scenario: OpenAPI gate

- **WHEN** the OpenAPI contract job runs
- **THEN** no step uses the Docker-based `oasdiff-action`, and the breaking diff keeps `--fail-on ERR --err-ignore backend-api/openapi/accepted-breaking-changes.txt`
