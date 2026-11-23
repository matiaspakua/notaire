# release-trains Specification

## Purpose

TBD - created by archiving change fix-2401-v08-release-train. Update Purpose after archive.
## Requirements
### Requirement: Every product minor has a release train

The product release check MUST find a train titled v<MAJOR>.<MINOR> · … for every released version, and MUST fail closed when none exists.

#### Scenario: release 0.8.0

- **WHEN** the product version is 0.8.0
- **THEN** the release check resolves the train 'v0.8 · Observable self-managing factory (2027-03)'

### Requirement: Every released minor has a release train

The product release check MUST find a train titled `v<MAJOR>.<MINOR> · …` for every released
product version, and MUST fail closed when none exists.

#### Scenario: release 0.9.0

- **WHEN** the product version is `0.9.0`
- **THEN** the release check resolves the train `v0.9 · Factory control plane (2027-03)`
- **AND** `check-product-version-coherence.py --tag v0.9.0` passes on the coherent tree

#### Scenario: release 0.8.0 stays addressable

- **WHEN** the product version is `0.8.0`
- **THEN** the release check resolves the train `v0.8 · Observable self-managing factory (2027-03)`

#### Scenario: unmapped minor still fails closed

- **WHEN** the product version is `0.10.0` and no `v0.10 · …` train exists
- **THEN** the release check fails with a message naming the missing train

### Requirement: release cuts are published once and repaired additively

A released minor with a coherent tree MUST have a `vX.Y.Z` tag and a GitHub Release carrying the
five documented assets; repairing a Release MUST NOT recreate it or overwrite an existing asset.

#### Scenario: missing 0.8.0 tag is published

- **WHEN** the `0.8.0` release commit exists without a `v0.8.0` tag
- **THEN** a `v0.8.0` tag is pushed at a 0.8.0-versioned commit whose tree passes coherence
- **AND** the GitHub Release contains the curated 0.8.0 changelog section

#### Scenario: assets are backfilled without clobber

- **WHEN** a Release is missing one of the five documented assets
- **THEN** any asset that can be derived from the tagged tree or a published image digest is
  uploaded with `gh release upload` without `--clobber`
- **AND** an image SBOM is omitted, with the omission recorded, when no image digest exists for
  the tag (never rebuilt or fabricated)
- **AND** the Release notes, published date and target commit remain unchanged

#### Scenario: pages history tracks the current cut

- **WHEN** the product version is `0.9.0`
- **THEN** the generated Pages release history reports `productVersion` `0.9.0` and lists both
  the `0.8.0` and `0.9.0` cuts

