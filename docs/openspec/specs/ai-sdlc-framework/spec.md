# ai-sdlc-framework Specification

## Purpose

TBD - created by archiving change chore-1958-generic-ai-sdlc-framework. Update Purpose after archive.

## Requirements

### Requirement: Gates are profile-driven commands with exit codes

The framework MUST run, for a named stage, every gate the profile declares for that stage, judge each gate only by its exit code against its `expect` value, skip a path-scoped gate when no changed file matches its globs, and exit non-zero when a required gate fails.

#### Scenario: a required gate fails

- **WHEN** a stage has a required gate whose command exits 1
- **THEN** `aisdlc gate <stage>` exits non-zero and names the gate

#### Scenario: a red gate expects failure

- **WHEN** a gate declares `expect = "fail"` and its command exits 1
- **THEN** the gate passes

#### Scenario: a path-scoped gate with no matching change

- **WHEN** a gate declares `paths` and no file changed against the base matches them
- **THEN** the gate is reported as skipped and does not fail the stage

### Requirement: Every gate run leaves evidence

The framework MUST append one JSON record per stage run to the evidence ledger with the stage, commit, start time, and for each gate its name, status, exit code and duration, and `aisdlc report` MUST summarize the ledger per gate.

#### Scenario: ledger after a stage run

- **WHEN** `aisdlc gate pre-push` runs two gates
- **THEN** the ledger gains one record with two gate results

#### Scenario: report over the ledger

- **WHEN** the ledger holds runs of a gate that passed once and failed once
- **THEN** the report shows two runs and a 50% pass rate for that gate

### Requirement: Baselines only shrink and floors only rise

The framework MUST fail a baseline ratchet when the working tree adds an entry that the base revision does not contain (line files and JSON count files), and MUST fail a floor ratchet when the measured value is below its floor.

#### Scenario: a hand-added baseline line

- **WHEN** a baseline file gains a line that is absent at the base revision
- **THEN** `aisdlc ratchet` exits non-zero and prints the added entry

#### Scenario: a shrunk baseline

- **WHEN** a baseline file loses a line
- **THEN** `aisdlc ratchet` passes

#### Scenario: a JSON suppression count grows

- **WHEN** a JSON baseline leaf count is larger than at the base revision
- **THEN** `aisdlc ratchet` exits non-zero

#### Scenario: a coverage floor is missed

- **WHEN** a floor ratchet's command prints a value below the floor
- **THEN** `aisdlc ratchet` exits non-zero

### Requirement: The skills catalog is linted

The framework MUST fail when a skill folder has no `SKILL.md`, when a skill has no frontmatter description, when two skills resolve to the same name, or when a skill the profile marks generic contains a pattern the profile forbids in generic skills.

#### Scenario: duplicate skill names

- **WHEN** two skill folders declare the same frontmatter name
- **THEN** `aisdlc skills` exits non-zero naming both folders

#### Scenario: a project path in a generic skill

- **WHEN** a generic skill references a forbidden project pattern
- **THEN** `aisdlc skills` exits non-zero naming the file

### Requirement: Always-loaded agent context stays within budget

The framework MUST estimate tokens of the always-loaded files as bytes divided by four and fail when the total exceeds the profile budget or a listed file is missing.

#### Scenario: over budget

- **WHEN** the always-loaded files exceed `max_tokens`
- **THEN** `aisdlc budget` exits non-zero and prints the total

#### Scenario: within budget

- **WHEN** the always-loaded files fit the budget
- **THEN** `aisdlc budget` exits zero

### Requirement: Commits and tests are checked against the base

The framework MUST fail when a commit subject in base..HEAD does not match the profile's commit pattern, and when production paths changed without any test path changing.

#### Scenario: non-conventional commit

- **WHEN** a commit subject is `update stuff`
- **THEN** `aisdlc commits` exits non-zero

#### Scenario: production change without tests

- **WHEN** only a production glob changed against the base
- **THEN** `aisdlc tdd` exits non-zero

### Requirement: Built-in change records are complete and traceable

The framework MUST scaffold a change record with the profile's required sections and fail `aisdlc change check` when a section holds only template comments, when no issue reference is present, or when the configured tracker command cannot resolve the issue.

#### Scenario: unfilled section

- **WHEN** a required section still holds only its template comment
- **THEN** `aisdlc change check` exits non-zero naming the section

#### Scenario: invented issue number

- **WHEN** the tracker verify command exits non-zero for the cited issue
- **THEN** `aisdlc change check` exits non-zero

### Requirement: The kit is independent of any adopter

The framework MUST NOT reference an adopter's paths outside adopter profiles, and MUST run end to end on a project with no Java, Maven or Node toolchain.

#### Scenario: stack-independent run

- **WHEN** `aisdlc init` scaffolds a fresh repository and `aisdlc doctor` and `aisdlc gate pre-push` run in it
- **THEN** both exit zero with only POSIX shell commands configured

#### Scenario: no adopter path in the kit

- **WHEN** the kit sources outside `profiles/` are scanned
- **THEN** none mention `notaire`, `backend-api` or `workspace/sdlc`

### Requirement: Notaire adopts the framework

Notaire MUST declare its profile at `.aisdlc/aisdlc.toml`, pass `aisdlc doctor`, and run the baseline ratchet and the skills lint in `preflight.sh` and in Process Checks.

#### Scenario: Notaire profile is valid

- **WHEN** `aisdlc --profile .aisdlc/aisdlc.toml doctor` runs at the repository root
- **THEN** it exits zero

#### Scenario: Process Checks run the ratchet

- **WHEN** a pull request adds a line to `hexagon-baseline.txt`
- **THEN** Process Checks fail on the baseline ratchet step

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
