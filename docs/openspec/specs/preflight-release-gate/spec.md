# preflight-release-gate Specification

## Purpose

TBD - created by archiving change chore-2493-fast-pr-preflight. Update Purpose after archive.

## Requirements

### Requirement: The fast preflight is the pull-request gate

The pre-push hook SHALL run `preflight.sh` in its default mode (format, lint, compile, typecheck, process checks,
and the unit tests and builds of the suite groups the branch touches). It SHALL NOT run the server-backed suites
(Playwright e2e, Bruno, Docker, database V&V) or the extraction-boundary scan unless `PREFLIGHT_FULL=1` or
`PREFLIGHT_ALL=1` is set.

#### Scenario: A docs-only branch pushes without the server-backed suites

- **WHEN** a branch that changes only `docs/` is pushed
- **THEN** the hook runs `preflight.sh` without `--full` and the summary says the pull-request gate passed

#### Scenario: A change to the gate runs its own tests only

- **WHEN** the branch changes `workspace/sdlc/preflight.sh` or `.githooks/pre-push`
- **THEN** the affected groups are empty and the process self-tests cover the change

### Requirement: The complete preflight is the release gate

`bash workspace/sdlc/preflight.sh --all` SHALL pass on the release commit before release-please is dispatched;
the version-tag CI repeats the heavy workflows.

#### Scenario: The release guide names the gate

- **WHEN** a maintainer follows `docs/300-development/RELEASE.md`
- **THEN** the first step is `bash workspace/sdlc/preflight.sh --all`

### Requirement: A pass is recorded once per commit and mode

`preflight.sh` SHALL record a pass for the checked-out commit when the working tree is clean; the hook SHALL accept
a pass of the same or a more complete mode (pr < full < all) and SHALL NOT accept a weaker one.

#### Scenario: A manual run covers the push

- **WHEN** `preflight.sh --full` passed on a clean tree and the same commit is pushed
- **THEN** the hook does not run the preflight again
