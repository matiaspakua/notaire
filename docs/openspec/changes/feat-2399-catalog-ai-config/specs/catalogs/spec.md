<!-- Governed by CONSTITUTION.md. Each `#### Scenario:` below IS an Acceptance
     Criterion (Gate 1) and must be traceable to a test in traceability.md.
     Business rules belong here in normative form (SHALL/MUST); the permanent
     Use Case documentation remains their source of truth - cite it, do not
     duplicate it. -->

## ADDED Requirements

### Requirement: Catalog import for harness AI configuration

The kit SHALL provide `aisdlc catalog import --harness <h> --from <dir> [--kind K] [--json]`
to import AI configuration assets (skills, agents, rules) from harness-specific layouts
into catalog entries. Files SHALL be read as raw bytes and decoded strictly as UTF-8,
preserving exact byte content and newline conventions (CRLF/LF). Non-UTF-8 files SHALL
be skipped and reported.

The import operation SHALL be idempotent: if all files for an existing catalog entry match
the current revision exactly, no new revision SHALL be created. If any file differs, a new
immutable revision SHALL be recorded via `catalogs.write_new_revision`.

Import MUST reject any file content matching literal secret patterns (`factory.SECRET_VALUES`),
raising a `KitError` with code `literal_secret` naming the file path without exposing secret
values. Entries rejected for secrets SHALL NOT be stored in the catalog, and the command SHALL
exit with code 1 after completing remaining imports.

#### Scenario: import stores files in a revision

- **WHEN** `aisdlc catalog import --harness claude-code --from <dir>` is run on a directory containing new agent or skill files
- **THEN** new catalog entries are created whose current revision includes a `files` mapping relative paths to exact UTF-8 file contents

#### Scenario: import is idempotent

- **WHEN** `aisdlc catalog import` is executed a second time against an unchanged harness directory
- **THEN** no new catalog revision is created and the command reports the entries as unchanged

#### Scenario: import rejects a literal secret

- **WHEN** an imported file contains text matching a credential pattern in `factory.SECRET_VALUES`
- **THEN** the entry is rejected, the secret value is not persisted, the error names the path without leaking the secret, and the command exits with code 1

### Requirement: Catalog materialization and drift checking

The kit SHALL provide `aisdlc catalog materialize --harness <h> --dest <dir> [--kind K] [--check] [--json]`
to project catalog entries into target harness directory structures. For every active catalog
entry supported by the harness layout containing a `files` payload, the kit SHALL write the
files under `--dest` matching the exact stored bytes without newline alteration.

The materialization process SHALL write a `.aisdlc-materialized.json` manifest at the destination
root containing schema version 1, target harness name, and sorted entries with kind, id, version,
catalog digest, and SHA-256 digests of each materialized file.

Existing files SHALL never be silently deleted; entries present in a prior manifest but missing
from current catalog entries SHALL be reported as stale.

When `--check` is specified, the command SHALL write no files to disk. It SHALL verify disk state
against expected catalog output, detecting missing files, content modifications (byte differences),
stale files, or a missing manifest as drift. If any drift is detected, the command SHALL print each
drift line and exit with code 1; if clean, it SHALL exit with code 0.

#### Scenario: materialize writes layout plus manifest

- **WHEN** `aisdlc catalog materialize --harness <h> --dest <dir>` is executed for a supported harness
- **THEN** all files are written into the harness layout and `.aisdlc-materialized.json` is generated with stable SHA-256 digests

#### Scenario: check reports drift and exits 1

- **WHEN** `aisdlc catalog materialize --harness <h> --dest <dir> --check` is run on a directory with modified, missing, or stale files
- **THEN** each drift item is reported and the command exits with code 1 without modifying disk

#### Scenario: materialize is evidence and telemetry

- **WHEN** `catalog import` or non-check `catalog materialize` executes successfully
- **THEN** an evidence record (`catalog.import` or `catalog.materialize`) is appended to the ledger, and a telemetry event is emitted with entry and file counts only, omitting file text and absolute paths

#### Scenario: round trip byte identical

- **WHEN** a multi-file skill folder, agent markdown, rule, and CRLF file fixture is imported from a harness layout and then materialized into a clean destination directory
- **THEN** all materialized files are byte-for-byte identical to the original input files

### Requirement: Harness stubs for agy and kiro

The kit SHALL include harness stub plugins for `agy` (Antigravity) and `kiro` under
`framework/ai-sdlc/plugins/agy` and `framework/ai-sdlc/plugins/kiro`, configured with runtime
descriptors, describe capabilities, sandbox set to false, and registered in `.aisdlc/aisdlc.toml`.

#### Scenario: agy and kiro stubs listed by harness list

- **WHEN** `aisdlc harness list` is executed
- **THEN** `agy` and `kiro` are displayed in the list of recognized harness adapters
