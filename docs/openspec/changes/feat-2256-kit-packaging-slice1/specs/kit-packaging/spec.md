<!-- Governed by CONSTITUTION.md. CU76, issue #2256. -->

## ADDED Requirements

### Requirement: Installable kit wheel

The kit SHALL build a wheel with a stdlib-only runtime, a console script `aisdlc` and a version equal to `aisdlc.__version__`. The wheel SHALL contain the templates, profiles, openapi, plugins and optional files, and `aisdlc install` run from an installed wheel SHALL produce a working kit copy and profile in the target repository.

#### Scenario: wheel installs and reports its version

- **WHEN** the wheel is installed into a clean virtual environment
- **THEN** `aisdlc --version` prints `aisdlc ` followed by `__version__`

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
