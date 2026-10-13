# native-stack Specification

## Purpose
TBD - created by archiving change feat-1665-native-stack. Update Purpose after archive.

## Requirements

### Requirement: A native script runs the CI stack without Docker

`workspace/stack/native.sh` MUST provide `up`, `down`, `status` and `env`. `up` MUST start PostgreSQL 16 with role and database `notaire`, the backend jar and the Next.js server using the datasource, JWT and actuator values of `playwright-e2e.yml`, MUST be a no-op when the stack runs, and MUST make the installed Chromium usable by Playwright. `down` MUST stop only what `up` started.

#### Scenario: backend flags equal the CI workflow

- **WHEN** the script's backend datasource, JWT and actuator values are compared with `playwright-e2e.yml`
- **THEN** they are equal

#### Scenario: usage lists every command

- **WHEN** the script runs with `--help`
- **THEN** it exits 0 and names `up`, `down`, `status` and `env`

#### Scenario: script is valid bash

- **WHEN** the script is parsed with `bash -n`
- **THEN** parsing succeeds

#### Scenario: status exit code reflects what is running

- **WHEN** `status` runs
- **THEN** it reports each service as up or down and exits 0 only when all are up
