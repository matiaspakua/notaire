<!-- Governed by CONSTITUTION.md. CU76, issue #1591. Observability baseline: ADR-016. -->

## Purpose

The running backend must report the same product version that was released,
taken from the artifact it ships in, and deployment descriptors must name the
released images without hand-edited tags.

## ADDED Requirements

### Requirement: Telemetry resource carries the packaged product version

The backend SHALL set the OpenTelemetry resource attribute `service.version`
to the `app.version` value packaged in the application. Every exported span and
metric SHALL inherit that attribute through the resource. The version SHALL NOT
be read from `release/product-release.json` at runtime, because that file is
not part of the backend image.

#### Scenario: Resource attribute equals the packaged version

- **WHEN** the application context starts with `app.version` set to `0.2.0`
- **THEN** the telemetry resource attribute `service.version` is `0.2.0` alongside the existing `service.name`

#### Scenario: Spans inherit the attribute

- **WHEN** a request produces a span that is exported
- **THEN** the exported span's resource contains `service.version` equal to `app.version`

#### Scenario: No runtime dependency on the record file

- **WHEN** the backend sources and the backend image build are inspected
- **THEN** neither reads nor copies `release/product-release.json`

#### Scenario: Version follows the Maven filter

- **WHEN** the backend resources are processed for a Maven version
- **THEN** the packaged `app.version` equals that Maven version, which the coherence check compares with the record

### Requirement: Structured logs carry the product version

Every JSON log line SHALL include the field `serviceVersion` equal to
`app.version`, next to the existing `service` and `environment` fields, so logs
can be correlated with the release without a per-request lookup.

#### Scenario: Log line has the version

- **WHEN** the backend writes a log line through the structured console appender
- **THEN** the JSON object contains `serviceVersion` equal to `app.version`

#### Scenario: Version is constant, not per-request state

- **WHEN** two requests are logged
- **THEN** both lines carry the same `serviceVersion` and no request header or body value is added to the log

### Requirement: Compose and Kustomize name the released images

`docker-compose.yml`, `docker-compose.prod.yml` and the Kustomize staging
overlay SHALL reference the backend and frontend GHCR images by the released
`X.Y.Z` tag equal to the product version, and the staging overlay SHALL label
workloads with `app.kubernetes.io/version` equal to it. release-please SHALL
rewrite those values from the same cut that bumps Maven and npm, so no tag is
edited by hand.

#### Scenario: Image tags equal the product version

- **WHEN** the compose files and the staging overlay are read
- **THEN** every backend and frontend GHCR reference ends in the product version

#### Scenario: Placeholder tags are rejected

- **WHEN** an image reference ends in `latest`, a SHA, or a placeholder
- **THEN** the coherence check exits non-zero

#### Scenario: Version label on staging workloads

- **WHEN** the staging overlay is rendered
- **THEN** backend and frontend pods carry `app.kubernetes.io/version` equal to the product version

#### Scenario: release-please covers every identity file

- **WHEN** the release-please configuration is read
- **THEN** its extra-files include both compose files and the staging overlay with the version marker, and every marked line is a semver
