# product-release

A Notaire release is one semver cut of the backend, the frontend, and the
database schema, aligned to a delivery train without using the milestone
title as the git tag. CU76, issue #1571.

## ADDED Requirements

### Requirement: The product record includes backend, frontend, and database

The committed product record SHALL list the Maven version, the npm version,
and the highest Flyway migration id. The Maven version, the npm version, and
the release-please manifest SHALL be the same semver. The schema id SHALL be
recorded and SHALL NOT be required to equal that semver.

#### Scenario: Versions on main match the manifest

- **WHEN** the product-release check runs on the tree
- **THEN** root `pom.xml`, the backend parent version, `frontend/package.json`, and the manifest share one semver, and that semver is not `1.0-SNAPSHOT`

#### Scenario: Database schema is a Flyway id

- **WHEN** the record is collected
- **THEN** `components.database.schemaVersion` is the highest `V*` migration and it differs from the product semver

### Requirement: A semver maps to a delivery train and is not the milestone title

Versions below 1.0.0 and versions 1.0.x SHALL map to the train
`v1.0 — Production Ready`. Versions 1.1.x SHALL map to `v1.1 — Hardening`.
Any other minor SHALL fail closed. A release SHALL NOT close catalog issues.

#### Scenario: Pre-1.0 and 1.0.x share the production train

- **WHEN** the train is derived for `0.2.0` and for `1.0.0`
- **THEN** both results are `v1.0 — Production Ready`

#### Scenario: An unmapped minor fails

- **WHEN** the train is derived for `1.2.0`
- **THEN** the derivation raises an error

### Requirement: New tags are vX.Y.Z and legacy component tags still publish

release-please SHALL set `include-component-in-tag` to false. CD SHALL run
the SBOM attach job for tags matching `v*` and for tags matching `notaire-v*`,
and SHALL attach the product bill and the database bill.

#### Scenario: Config and workflow accept both generations of tag

- **WHEN** the release-please config and `cd.yml` are read
- **THEN** `include-component-in-tag` is false, both tag filters are present, and the attach step names `sbom-notaire.cdx.json` and `sbom-database.cdx.json`

### Requirement: The product SBOM lists the three components

The product CycloneDX document SHALL name `notaire-backend`,
`notaire-frontend`, and `notaire-database`, and SHALL carry the release train
as a property. The database document SHALL list every versioned migration file.

#### Scenario: Product bill composition

- **WHEN** the product SBOM is generated from the tree
- **THEN** its component names are the backend, the frontend, and the database, and the database component version is the schema id

#### Scenario: Database bill lists migrations

- **WHEN** the database SBOM is generated
- **THEN** it contains one file component per versioned Flyway migration and the metadata version is the schema id

### Requirement: Version history lists semver cuts only

The Pages history SHALL list release-please changelog headings and SHALL NOT
treat the legacy `1.0.0-SNAPSHOT` heading as a release.

#### Scenario: Snapshot heading is ignored

- **WHEN** changelog headings are parsed
- **THEN** `0.1.2` and `0.1.1` are present and `1.0.0-SNAPSHOT` is absent

### Requirement: The running system shows the product version

Actuator info SHALL expose the product version and the database schema.
OpenAPI `info.version` SHALL be the injected product version. The frontend
sidebar SHALL show the `package.json` version.

#### Scenario: Actuator section has three components

- **WHEN** the info contributor runs with product version `0.1.2` and schema `46`
- **THEN** the `notaire` detail contains that version and `databaseSchema` `46`

#### Scenario: OpenAPI uses the injected version

- **WHEN** OpenAPI is built with version `0.1.2`
- **THEN** `info.version` is `0.1.2`

#### Scenario: Sidebar shows the package version

- **WHEN** the sidebar is rendered
- **THEN** the version element shows the `package.json` version
