# context-budget Specification

## Purpose
Wire memory catalog pack ids into the always-loaded context budget so profile
`[context]` can reference packs, validate fail-closed, and measure pack refs
without executing them.

## Requirements

### Requirement: Optional memory pack refs in context budget config
The profile `[context]` table SHALL accept an optional `memory` list of memory
catalog pack ids. Omitted or empty `memory` SHALL be a no-op for validation
(gates and `aisdlc budget` still use `files`/`max_tokens` when those are set).
`aisdlc budget` SHALL be considered configured when `max_tokens` is set and at
least one of `files` or `memory` is non-empty.

#### Scenario: omitted memory is no-op
- **WHEN** a profile has `[context]` files/max_tokens but no `memory` list
- **THEN** `config validate` and `doctor` succeed without requiring memory packs

#### Scenario: budget configured via memory packs only
- **WHEN** `[context]` sets `max_tokens` and `memory` with a valid pack whose ref exists, and `files` is empty
- **THEN** `aisdlc budget` measures the pack ref and exits 0 when under the limit

### Requirement: Memory pack ids resolve fail-closed
Pack ids in `[context].memory` SHALL resolve against `[catalogs]` memory entries
(same listing surface as `catalog list --kind memory`). An unknown id SHALL make
`aisdlc config validate` and `aisdlc doctor` fail (never silent pass). When
`[catalogs] dir` is unset/missing and `memory` is non-empty, validate/doctor
SHALL error.

#### Scenario: unknown memory pack fails validate
- **WHEN** `[context].memory` names a pack id absent from the memory catalog
- **THEN** `config validate` exits non-zero naming the unknown id

#### Scenario: unknown memory pack fails doctor
- **WHEN** `[context].memory` names a pack id absent from the memory catalog
- **THEN** `doctor` reports an ERROR and exits non-zero

#### Scenario: missing catalogs dir with memory ref fails
- **WHEN** `[context].memory` is non-empty and `[catalogs] dir` is unset
- **THEN** `config validate` exits non-zero (never silent pass)

### Requirement: Budget measures files and memory pack refs
`aisdlc budget` SHALL estimate tokens as total bytes of measured paths divided
by 4. Measured paths SHALL be the union of `[context].files` and each resolved
memory pack `ref` (file → that file; directory → non-hidden regular files one
level deep), deduplicated by path. A missing pack entry, missing `ref`, or
missing ref path SHALL fail the budget check.

#### Scenario: budget includes pack ref bytes
- **WHEN** a valid memory pack ref points at a file not already listed in `files`
- **THEN** `aisdlc budget` includes that file's bytes in the token estimate

#### Scenario: missing pack ref fails budget
- **WHEN** a memory pack's `ref` path does not exist
- **THEN** `aisdlc budget` exits non-zero naming the pack or path

### Requirement: Context budget surfaces never execute packs
`aisdlc budget`, `config validate`, `config show --redacted`, `catalog list`,
and `catalog validate` SHALL only read profile/catalog metadata and measure file
sizes. They MUST NOT execute memory pack payloads (ref/summary strings).

#### Scenario: hostile pack summary never executes
- **WHEN** a memory pack summary contains a shell command string and `budget` /
  `config validate` / `catalog list --kind memory` run
- **THEN** those commands exit without executing the command string

#### Scenario: config show includes context memory
- **WHEN** `config show --redacted --json` runs with `[context].memory` set
- **THEN** the JSON includes a `context` object with the `memory` pack ids
