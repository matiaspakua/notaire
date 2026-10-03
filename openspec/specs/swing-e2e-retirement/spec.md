# swing-e2e-retirement Specification

## Purpose

Keep Swing desktop E2E permanently retired: no resurrected workflow, no Maven
build of removed Swing modules in GitHub Actions, and no live docs that teach
operators to run Robot Framework against a Swing client that no longer exists.

## Requirements

### Requirement: Swing E2E workflow stays retired

The repository MUST NOT contain `.github/workflows/e2e-swing.yml`. ADR-012
already records retirement; hygiene MUST fail if the file returns.

#### Scenario: e2e-swing workflow remains absent

- **WHEN** Dependabot/Swing hygiene runs against the repository tip
- **THEN** `.github/workflows/e2e-swing.yml` is not present as a file

#### Scenario: synthetic e2e-swing workflow fails hygiene

- **WHEN** a temporary fixture creates `.github/workflows/e2e-swing.yml`
- **THEN** the hygiene assert for workflow absence fails

### Requirement: Workflows must not build removed Swing modules

GitHub Actions workflow YAML under `.github/workflows/` MUST NOT instruct Maven
to build `frontend-swing` or `deprecated-frontend-swing` (for example via
`-pl frontend-swing` or `-pl deprecated-frontend-swing`).

#### Scenario: workflows do not build Swing modules

- **WHEN** hygiene scans all `.github/workflows/*.yml` files at the tip
- **THEN** none contain a Maven `-pl` invocation for `frontend-swing` or
  `deprecated-frontend-swing`

#### Scenario: synthetic Swing Maven build fails hygiene

- **WHEN** a temporary fixture workflow contains `-pl frontend-swing` or
  `-pl deprecated-frontend-swing`
- **THEN** the hygiene assert for Swing builds fails

### Requirement: Robot Swing suite is hard-deprecated without CI wiring

`testing/e2e-swing/` MAY remain on disk for historical assets and ignore-rule
hygiene, but MUST be clearly hard-deprecated and MUST NOT be invoked from CI.

#### Scenario: e2e-swing suite hard-deprecated

- **WHEN** an operator opens `testing/e2e-swing/README.md` or runs
  `testing/e2e-swing/run_tests.sh`
- **THEN** the suite is documented or exits as retired, forbidding CI wiring
