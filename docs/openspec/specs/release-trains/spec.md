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

### Requirement: The 0.10 minor has a release train

The product release check MUST resolve the train `v0.10 · Factory production line & delivery gates (2027-03)` for every `0.10.z` version, and MUST still fail closed for a minor with no train.

#### Scenario: release 0.10.0

- **WHEN** the product version is `0.10.0`
- **THEN** the release check resolves the train `v0.10 · Factory production line & delivery gates (2027-03)`
- **AND** `check-product-version-coherence.py --tag v0.10.0` passes on a coherent `0.10.0` tree

#### Scenario: an unmapped minor still fails closed

- **WHEN** the product version is `1.1.0` and no `v1.1 · …` train exists
- **THEN** the release check fails with a message naming the missing train

### Requirement: The release branch is gated on its train

The Release Please job MUST run `check-product-version-coherence.py --tag v<next version>` on the release branch after it syncs the OpenAPI version, so a cut without a train fails before its tag exists.

#### Scenario: next cut has no train

- **WHEN** Release Please updates the release PR for a minor with no train
- **THEN** the Release Please job fails naming the missing train
- **AND** no tag is created until the train exists

### Requirement: The next minor's train exists before a release is cut

The release trains SHALL always include the train for the minor after the current release
(`.release-please-manifest.json`), so a release-please PR can never cut a version without its train.

#### Scenario: the next train is missing

- **GIVEN** the manifest at `0.11.0` and no `v0.12 · …` train
- **WHEN** the release tests run
- **THEN** `test_the_next_minor_already_has_a_train` fails

#### Scenario: v0.11 resolves its train

- **WHEN** `train_for("0.11.0")` runs
- **THEN** it returns `v0.11 · Factory evidence & kit-only SDLC (2027-03)`

### Requirement: Pages CI regenerates its data before the tests

Pages CI SHALL run the prebuild generators (release history, testing inventory, RepoLens bench snapshot) before
Vitest, and the RepoLens Pages test SHALL derive its headline labels from the committed bench JSON.

#### Scenario: a release lands before the snapshot is regenerated

- **GIVEN** a manifest one release ahead of the committed `releases.json`
- **WHEN** Pages CI runs
- **THEN** the data is regenerated first and `pages-status.test.ts` passes

#### Scenario: a re-measured latency

- **GIVEN** a refreshed `retrieval.json` with a new latency ratio
- **WHEN** `repolens-pages.test.ts` runs
- **THEN** the label it expects is computed from that JSON
