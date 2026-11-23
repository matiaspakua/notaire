# preflight-environment Specification

## Purpose

TBD - created by archiving change fix-2442-preflight-e2e-deps. Update Purpose after archive.

## Requirements

### Requirement: Playwright runs with its dependencies installed

`preflight.sh` SHALL install `testing/e2e` dependencies when they are missing before any step that runs
the e2e suite's binaries, including the server-backed Playwright run.

#### Scenario: a fresh worktree reaches the server-backed suites

- **GIVEN** a worktree without `testing/e2e/node_modules`
- **WHEN** a full preflight reaches the Playwright step
- **THEN** the dependencies are installed first and Playwright runs

### Requirement: The OpenAPI types check leaves no file behind

`npm run openapi:types:check` SHALL remove its temporary file whether the comparison passes or fails, and
SHALL still exit non-zero when the types differ.

#### Scenario: the generated types are stale

- **GIVEN** a spec whose types differ from `src/types/api.generated.ts`
- **WHEN** the check runs
- **THEN** it exits non-zero and `src/types/api.generated.check.ts` does not exist
