# kit-packaging Specification

## Purpose

TBD - created by archiving change feat-2256-kit-packaging-slice1. Update Purpose after archive.

## Requirements

### Requirement: Installable kit wheel

The kit SHALL build a wheel with a stdlib-only runtime, a console script `aisdlc` and a version equal to `aisdlc.__version__`. The wheel SHALL contain the templates, profiles, openapi, plugins and optional files, and `aisdlc install` run from an installed wheel SHALL produce a working kit copy and profile in the target repository.

#### Scenario: wheel installs and reports its version

- **WHEN** the wheel is installed into a clean virtual environment
- **THEN** `aisdlc --version` prints `aisdlc` followed by `__version__`

#### Scenario: install from the wheel yields a healthy repository

- **WHEN** `aisdlc install` runs from the installed wheel inside a git repository and `aisdlc doctor` follows
- **THEN** the kit tree has `bin/aisdlc` and `templates/` and doctor exits 0

#### Scenario: wheel excludes adopter content and tests

- **WHEN** the wheel file list is read
- **THEN** it has no path under `tests/` and no Notaire path

### Requirement: Kit version skew report

The profile MAY record `[project] kit_version`. `aisdlc doctor` SHALL compare it with the running kit and SHALL NOT fail the run because of a difference.

#### Scenario: matching version

- **WHEN** `kit_version` equals the running version
- **THEN** doctor prints no skew warning

#### Scenario: minor or patch difference is informational

- **WHEN** `kit_version` differs in minor or patch only
- **THEN** doctor prints an INFO line naming both versions and exits 0

#### Scenario: major difference warns

- **WHEN** `kit_version` has another major version
- **THEN** doctor prints a WARN line naming both versions and exits 0

#### Scenario: absent or invalid key

- **WHEN** `kit_version` is absent
- **THEN** doctor prints no skew line; when it is not `MAJOR.MINOR.PATCH`, the profile is rejected

### Requirement: Kit tag release with SBOM and provenance

On a push of a tag matching `aisdlc-v*`, the kit release workflow SHALL build a wheel and an sdist, attach a CycloneDX SBOM and a GitHub artifact provenance attestation to the GitHub Release for those artifacts, and SHALL NOT publish to PyPI unless the Owner later asks.

#### Scenario: release workflow declares kit tag build and attest

- **WHEN** the kit CI workflow file is read
- **THEN** it triggers on `aisdlc-v*` tags, builds wheel and sdist under `framework/ai-sdlc`, uploads them with an SBOM asset, and includes a provenance attestation step; PyPI publish steps are absent

### Requirement: Conformance matrix for stock profiles

For each stock profile name `go-service`, `java-maven`, `node-web` and `python-service`, a conformance fixture SHALL install the built kit wheel into a clean environment and run `install → doctor → change new → gate plan → gate red → report`, asserting the expected exit codes.

#### Scenario: each profile matrix row exits successfully through report

- **WHEN** the conformance matrix runs for a stock profile fixture
- **THEN** install, doctor, change new, gate plan, gate red and report each exit 0

### Requirement: Second-adopter wheel proof

A non-Notaire sample project SHALL adopt the kit via the installed wheel. Time from empty repo to first verified gate (`gate plan` exit 0) SHALL be measured and SHALL be ≤ 15 minutes.

#### Scenario: second adopter reaches first verified gate within fifteen minutes

- **WHEN** the second-adopter sample is installed from the built wheel and runs through doctor and gate plan
- **THEN** gate plan exits 0 and the recorded elapsed seconds are ≤ 900

### Requirement: Profile schema_version

The profile MAY record `[project] schema_version` as a positive integer. `aisdlc init` and `aisdlc install` SHALL write the current profile schema version. An invalid value SHALL fail profile load. Absence SHALL be treated as schema version 1 for compatibility.

#### Scenario: init records schema_version

- **WHEN** `aisdlc init` scaffolds a profile
- **THEN** the profile contains `[project] schema_version` equal to the kit's current profile schema version

#### Scenario: invalid schema_version is rejected

- **WHEN** the profile sets `schema_version` to a non-positive or non-integer value
- **THEN** profile load fails naming `schema_version`

### Requirement: Kit changelog and upgrade policy

The kit SHALL ship a kit-local `CHANGELOG.md` and an upgrade document that states a deprecation window of one minor version. PyPI publishing SHALL be documented as an Owner decision / non-goal until asked.

#### Scenario: changelog and upgrade notes exist

- **WHEN** the kit packaging docs are inspected
- **THEN** `CHANGELOG.md` and `docs/UPGRADE.md` exist under `framework/ai-sdlc` and the upgrade doc names the one-minor deprecation window and the PyPI non-goal

### Requirement: Independence preserved

The independence scan SHALL still pass: kit code and templates SHALL NOT contain adopter product path markers.

#### Scenario: independence scan stays clean

- **WHEN** the independence test runs after packaging remainders land
- **THEN** it finds no adopter markers under `aisdlc/`, `bin/` or `templates/`
