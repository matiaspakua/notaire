<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md. -->

## Purpose

Make `testing/` a single, self-contained, documented folder with which a separate QA team
verifies and validates Notaire as black-box modules, ready to be split into its own
repository. Source: #1191 (umbrella #1190); owner CU76.

## ADDED Requirements

### Requirement: System-level suites live under testing/

The integration and database suites, the runner and the guides MUST live under `testing/`. `testing/e2e-swing/` MUST remain untouched. Scripts that nothing references and
the stale committed reports MUST NOT remain.

#### Scenario: Suites live under testing/

- **WHEN** the repository tree is inspected
- **THEN** `testing/integration`, `testing/database`, `testing/scripts/run.sh`, `testing/.env.example`, `testing/README.md` and
  `testing/docs/{PREPARATION,CONFIGURATION,DEFINITION,OPERATION}.md` exist

#### Scenario: Dead scripts and stale reports are gone

- **WHEN** the repository tree is inspected
- **THEN** `testing/run-all-tests.sh`, `testing/scripts/test-all.sh`,
  `testing/scripts/run-comprehensive-tests.sh`, `testing/generate-coverage-report.sh` and
  `testing/reports/*.md` do not exist, and `testing/e2e-swing/README.md` still does

### Requirement: One runner

`testing/scripts/run.sh <suite>` MUST run a named suite (`integration`, `database`), print the suites with `--list`, and exit non-zero for an unknown suite.
`testing/scripts/test.sh` MUST keep working as the integration entry point.

#### Scenario: Runner lists suites and rejects unknown ones

- **WHEN** `run.sh --list` and `run.sh nonsense` are executed
- **THEN** the first prints `integration` and `database` and exits 0, and the second exits
  non-zero

#### Scenario: test.sh still runs the integration suite

- **WHEN** `testing/scripts/test.sh` is inspected
- **THEN** it delegates to `run.sh integration` and no longer mentions any removed script

### Requirement: Database suite verifies an empty database up to the latest migration

The database suite MUST start an empty PostgreSQL, apply the application's Flyway migrations
with the Flyway CLI (read-only mount, no Spring context) and verify the result with SQL. It
MUST supply the same Flyway placeholders the backend does (`exporterUsername`,
`exporterPassword`), because migration V12 cannot run without them.

#### Scenario: Migrations apply to an empty database

- **WHEN** the suite runs `flyway migrate` on an empty database
- **THEN** every versioned migration is applied, `flyway_schema_history` has no failed row and
  `flyway validate` succeeds

#### Scenario: Only the documented rollback script is ignored

- **WHEN** the suite compares the SQL files with the Flyway history
- **THEN** every `V<n>__*.sql` file is applied, and the only file Flyway ignores is the manual
  rollback script `R14__restore_presupuestos_fk_id_tramite.sql`

#### Scenario: A second migrate is a no-op

- **WHEN** `flyway migrate` runs again on the migrated database
- **THEN** it applies nothing and the history is unchanged

#### Scenario: An edited migration is detected

- **WHEN** a copy of an already-applied migration is modified and `flyway validate` runs against it
- **THEN** validation fails with a checksum mismatch

### Requirement: Database suite checks configuration, roles, seed data and schema

After migration the suite MUST verify the server configuration, the least-privilege exporter
role, the seeded reference data and the presence of the core schema objects.

#### Scenario: Server configuration is as required

- **WHEN** the suite queries the server
- **THEN** database encoding is UTF8 and the server version is 16

#### Scenario: The exporter role is least-privilege

- **WHEN** the suite inspects the role created by migration V12
- **THEN** it exists, can log in, is a member of `pg_monitor` only and is not a superuser

#### Scenario: Seed data is present

- **WHEN** the suite counts the rows loaded by the seed migrations (V2, V10)
- **THEN** each seeded table holds at least the rows the migrations insert, including the default
  administrator user, the identification, document, folio and management-status reference data,
  and the demo workflow (definition, nodes, transitions)

#### Scenario: Core schema objects exist

- **WHEN** the suite inspects the migrated schema
- **THEN** every table has a primary key, foreign keys exist and are validated, and no table
  renamed by a migration (`ALTER TABLE x RENAME TO y`) still exists under its old name

### Requirement: The database suite runs isolated and pinned

The suite MUST use pinned images, mount the migrations read-only and publish no host port, so
it cannot collide with a running stack.

#### Scenario: The suite uses pinned images and publishes no host port

- **WHEN** the database compose file is inspected
- **THEN** its images are `postgres:16.15` and `flyway/flyway:12.4.0`, the migrations are
  mounted read-only from a variable, and no service publishes a host port

### Requirement: testing/ is self-contained and documented

Files under `testing/` (excluding `e2e-swing/`) MUST NOT reference paths outside it, except the
documented seams marked in the source. `testing/.env.example` MUST list every variable the
suites use and contain no real credential.

#### Scenario: testing/ does not reference paths outside itself

- **WHEN** scripts and compose files under `testing/` are scanned
- **THEN** none resolves a path above `testing/`, except lines carrying the `TESTING_APP_SEAM`
  marker

#### Scenario: Environment example is complete and secret-free

- **WHEN** `testing/.env.example` is read
- **THEN** it declares each variable referenced by the suites and holds only placeholder values

#### Scenario: Documentation set exists and docs link to it

- **WHEN** the documentation is inspected
- **THEN** the four guides exist, `testing/README.md` links each, and
  `docs/300-development/303-testing/README.md` links to `testing/`

### Requirement: Gates stay in sync

A gate added to CI MUST be added to `scripts/preflight.sh` in the same change, so local and CI
never drift.

#### Scenario: CI and preflight carry the database gate

- **WHEN** `.github/workflows/database-vv.yml` and `scripts/preflight.sh` are inspected
- **THEN** the workflow runs the database suite on changes to migrations or `testing/database`,
  and `preflight.sh --full` and `--list` include the same gate
