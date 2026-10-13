<!-- Governed by CONSTITUTION.md. CU76, issue #1591. Predecessor: #1571 / PR #1572. -->

## Purpose

The product version, the Flyway schema id, the release tag, the generated
CycloneDX documents and the image identity must agree, and a disagreement must
stop local preflight and CI before anything is published.

## ADDED Requirements

### Requirement: One canonical check compares every version source

The coherence check SHALL compare the root Maven version, the backend parent
version, the frontend npm version, the release-please manifest, the committed
product record, the highest Flyway migration id, and the newest changelog
release. It SHALL exit non-zero and name both disagreeing sources when any pair
differs, and exit zero on a coherent tree. The database schema id SHALL be
compared with the migrations, not with the semver.

#### Scenario: Coherent tree passes

- **WHEN** the check runs on a tree whose Maven, npm, manifest, record and changelog share one semver and whose record schema id is the highest migration
- **THEN** it exits 0 and prints the product version

#### Scenario: Maven and npm drift fails

- **WHEN** the root Maven version differs from the frontend npm version
- **THEN** it exits non-zero and the message names both files and both versions

#### Scenario: Backend parent drift fails

- **WHEN** the backend parent version differs from the manifest
- **THEN** it exits non-zero and names the backend module manifest

#### Scenario: Stale database schema in the record fails

- **WHEN** a versioned migration is added and the committed record still lists the previous schema id
- **THEN** it exits non-zero and tells the author how to regenerate the record

#### Scenario: Changelog behind the manifest fails

- **WHEN** the newest changelog release is not the manifest version
- **THEN** it exits non-zero

#### Scenario: Snapshot version fails

- **WHEN** the Maven version is a snapshot
- **THEN** it exits non-zero

### Requirement: Release tags are validated against the tree

When given a tag, the check SHALL accept `vX.Y.Z` only when `X.Y.Z` equals the
tree version, and SHALL accept `notaire-vX.Y.Z` only for the legacy versions
`0.1.1` and `0.1.2`. It SHALL reject every other shape, including a missing
prefix, a two-part version, a pre-release suffix, and a legacy prefix on a
later version. A legacy tag SHALL be validated for shape only and SHALL NOT be
compared with the current tree.

#### Scenario: Matching tag passes

- **WHEN** the check runs with the tag `v0.2.0` on a tree at `0.2.0`
- **THEN** it exits 0

#### Scenario: Tag ahead of the tree fails

- **WHEN** the check runs with the tag `v0.2.1` on a tree at `0.2.0`
- **THEN** it exits non-zero and names the tag and the tree version

#### Scenario: Legacy tag keeps working

- **WHEN** the check runs with the tag `notaire-v0.1.2`
- **THEN** it exits 0 without comparing Maven or npm versions of the current tree

#### Scenario: Legacy prefix on a new version fails

- **WHEN** the check runs with the tag `notaire-v0.2.0`
- **THEN** it exits non-zero because new releases use the `v` prefix

#### Scenario: Malformed tags fail

- **WHEN** the check runs with `0.2.0`, `v0.2`, `v0.2.0-rc.1`, or `release-v0.2.0`
- **THEN** each exits non-zero

### Requirement: Generated SBOM documents and the asset list cannot drift

The check SHALL generate the product and database CycloneDX documents in
memory and SHALL fail when their component versions differ from the Maven and
npm versions or the schema id, when the database bill does not list every
versioned migration, or when the asset list attached by the release job is not
exactly the five documented assets.

#### Scenario: Generated bills match the tree

- **WHEN** the product and database documents are generated from a coherent tree
- **THEN** the product bill lists backend and frontend at the semver and the database at the schema id, and the database bill has one file per versioned migration

#### Scenario: Attach list missing an asset fails

- **WHEN** the release workflow's attach list omits one of `sbom-notaire.cdx.json`, `sbom-database.cdx.json`, `sbom-backend.cdx.json`, `sbom-frontend.cdx.json`, `product-release.json`
- **THEN** the check exits non-zero and names the missing asset

#### Scenario: Extra asset name fails

- **WHEN** the attach list names a sixth file
- **THEN** the check exits non-zero because the release contract is exactly five assets

### Requirement: The packaged application version is validated against the record

The check SHALL accept the path of the packaged `application.properties` and
SHALL fail when its `app.version` is absent, is an unresolved filter token, or
differs from the record version. The check SHALL NOT require the record file to
be present at runtime.

#### Scenario: Packaged version matches

- **WHEN** the packaged properties contain `app.version` equal to the record version
- **THEN** the check exits 0

#### Scenario: Unfiltered token fails

- **WHEN** the packaged properties contain the literal `@project.version@`
- **THEN** the check exits non-zero and says the resource was not filtered

#### Scenario: Packaged version drift fails

- **WHEN** the packaged `app.version` differs from the record version
- **THEN** the check exits non-zero and names both values

### Requirement: The check blocks locally and in CI without credentials

The check SHALL run as a named blocking step in local preflight and, through
it, in the full pre-PR pipeline; in the pull-request process checks; after the
backend build in CI; and before any image is built in CD. It SHALL read only
the working tree and SHALL NOT call `gh`, the network, or read a GitHub token.

#### Scenario: Preflight and pipeline invoke the check

- **WHEN** preflight and the pre-PR pipeline definitions are read
- **THEN** preflight runs the check unconditionally and the pipeline reaches it through `preflight.sh --full`

#### Scenario: Auth-free execution

- **WHEN** the check runs with no `GH_TOKEN` or `GITHUB_TOKEN` in the environment and a `gh` on the path that fails when called
- **THEN** it exits 0 on a coherent tree and `gh` was never called

#### Scenario: CI runs the same command

- **WHEN** the process-check, build and CD workflows are read
- **THEN** each invokes the same script, and CD runs it before any image build and passes the pushed tag when the ref is a tag

#### Scenario: A release-please cut stays coherent

- **WHEN** every file listed in the release-please extra-files is bumped to the next version
- **THEN** the check passes on the bumped tree with the matching `v` tag, proving the configured sources cover every compared source
