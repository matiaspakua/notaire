<!-- Governed by CONSTITUTION.md. CU76, issue #2052. -->

## ADDED Requirements

### Requirement: Owner-only paths are checked on the diff

The framework MUST fail `aisdlc protected` when a file changed since the base matches a `[protected] paths` glob, naming each path and the owner who merges, and MUST pass when none matches or none is configured.

#### Scenario: a policy file changed

- **WHEN** the diff modifies a path listed in `[protected] paths`
- **THEN** `aisdlc protected` exits 1 and names the path and the owner

#### Scenario: an ordinary change

- **WHEN** no changed path matches `[protected] paths`
- **THEN** `aisdlc protected` exits 0

### Requirement: The review budget is checked on the diff

The framework MUST count added plus deleted lines since the base, excluding `[size] exclude` globs and binary files, and MUST fail `aisdlc size` when the count exceeds the budget.

#### Scenario: over the budget

- **WHEN** the counted lines exceed `[size] limit`
- **THEN** `aisdlc size` exits 1 and lists the largest files

#### Scenario: excluded files do not count

- **WHEN** only excluded paths make the diff large
- **THEN** `aisdlc size` exits 0 and reports the excluded count

### Requirement: The diff stays inside the declared write set

The framework MUST read the `[scope] heading` section of every change record the diff touches, ignoring HTML comments, and MUST fail `aisdlc scope` when a changed path outside the change records and `[scope] always` matches no write-set entry; with no write set it MUST report NOT EVALUABLE, and fail under `--strict`.

#### Scenario: a path outside the write set

- **WHEN** the diff changes a path no write-set entry covers
- **THEN** `aisdlc scope` exits 1 and names the path

#### Scenario: no write set declared

- **WHEN** no change record in the diff declares a write set
- **THEN** `aisdlc scope` reports NOT EVALUABLE and exits 0, or exits 1 with `--strict`

### Requirement: Evidence is hash-chained and anchorable

Every ledger and cost record MUST carry `prev`, the SHA-256 of the line before it; `aisdlc evidence verify` MUST fail on an edited, deleted or reordered record, MUST fail when an `--anchor` digest is no longer in the ledger, and MUST NOT report an empty ledger as passed.

#### Scenario: an edited record

- **WHEN** a ledger line is changed after it was written
- **THEN** `aisdlc evidence verify` exits 1 and names the line after it

#### Scenario: a truncated tail

- **WHEN** the anchored head record has been removed
- **THEN** `aisdlc evidence verify --anchor <digest>` exits 1

#### Scenario: an empty ledger

- **WHEN** the ledger has no records
- **THEN** `aisdlc evidence verify` reports NO EVIDENCE, not PASSED
