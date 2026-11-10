# grants Specification

## Purpose
Optional Grant profile schema (V1 axiom 5): worktree, write set, required checks, and
budget — validate/list/show plus write-set policy dry-run (`grant check`). Data and
lite policy toward Isolator OS; never a sandbox claim.

## Requirements

### Requirement: Optional grants in the profile
The profile SHALL accept optional `[[grants]]` entries. Each entry SHALL declare a
non-empty `id` and MAY declare `worktree` (string), `write_set` (list of path globs),
`required_checks` (list of gate names), `budget` (object with the same units as stage
effort: `tokens`, `time_seconds`, `questions`, `review_lines`), optional `stage`
(must match a declared gate stage when set), optional `expires_at` (ISO-8601 UTC
timestamp string), and optional `ttl_seconds` (positive integer). Omitted or empty
grants SHALL be a no-op. Expire fields are kill-switch data toward Isolator OS —
they MUST NOT alone claim sandbox isolation.

#### Scenario: omitted grants are no-op
- **WHEN** a profile has gates but no `[[grants]]` tables
- **THEN** `config validate` and `doctor` succeed without requiring grants

#### Scenario: parse grant fields
- **WHEN** a profile declares `[[grants]]` with id, worktree, write_set, required_checks,
  budget, and a valid stage
- **THEN** the kit loads those fields and surfaces them in `config show --redacted` /
  `grant list --json`

#### Scenario: unknown stage on grant fails load
- **WHEN** a grant sets `stage` to a value that does not match any gate stage
- **THEN** profile load fails with a configuration error (exit 2)

#### Scenario: grant expire fields parse and show
- **WHEN** a profile declares `[[grants]]` with `expires_at` and/or `ttl_seconds`
- **THEN** the kit loads those fields and surfaces them in `grant list --json` /
  `grant show --json` / `config show --redacted`

### Requirement: Required checks fail closed against gates
When a grant declares a non-empty `required_checks` list, each entry SHALL match a
declared `[[gates]].name`. An unknown check name SHALL make profile load or
`aisdlc config validate` / `aisdlc doctor` fail (never silent pass).

#### Scenario: unknown required check fails validate
- **WHEN** a grant lists a required_checks name absent from declared gates
- **THEN** `config validate` exits non-zero naming the unknown check

### Requirement: Grant list and show never enforce
`aisdlc grant list` / `grant list --json` / `grant show --id` and `config show --redacted`
SHALL only read profile data. They MUST NOT enforce write sets, expire grants, claim
sandbox isolation, or change Isolator status.

#### Scenario: grant list json never claims sandbox
- **WHEN** `grant list --json` runs with a declared grant
- **THEN** the command exits 0, prints grant fields, and does not set Isolator
  `sandbox=true` or Isolator status other than `no-evaluable`

#### Scenario: isolator stays no-evaluable with grants present
- **WHEN** a profile declares grants and `aisdlc doctor` runs
- **THEN** Isolator INFO remains `no-evaluable` with `sandbox=false` and doctor does not
  fail solely because grants exist

### Requirement: Grant write-set policy dry-run
The kit SHALL provide `aisdlc grant check [--base] [--json]` that compares changed
paths since `base` (default profile base) against the union of declared Grant
`write_set` globs. Matching SHALL use the same path-glob honesty as scope write-set
checks (`repo.matches` / directory expansion). This is a **policy** dry-run only:
it MUST NOT claim OS isolation, sandbox, bubblewrap, landlock, or Isolator runtime
enforcement.

#### Scenario: no grant declared is no-evaluable
- **WHEN** a profile has no `[[grants]]` (or every grant has an empty `write_set`)
- **THEN** `grant check --json` exits 0 with status `no-evaluable` and does not claim sandbox

#### Scenario: paths inside write_set pass
- **WHEN** a profile declares a Grant with a non-empty `write_set` and every changed
  path since base matches those globs
- **THEN** `grant check --json` reports status `pass`

#### Scenario: path outside write_set fails
- **WHEN** at least one changed path since base is outside every Grant `write_set`
- **THEN** `grant check --json` reports status `fail` and names the outside path(s)

#### Scenario: doctor denies sandbox for grant check
- **WHEN** `aisdlc doctor` runs with or without grants declared
- **THEN** doctor INFO states Grant write-set check is policy-only / not a sandbox,
  and Isolator remains `no-evaluable` with `sandbox=false`

### Requirement: Grant expire fails closed
The kit SHALL evaluate Grant expire when `expires_at` and/or `ttl_seconds` is set.
A grant is expired when `expires_at` is in the past (UTC) or when `ttl_seconds` is
set and the evaluation clock is at or beyond the grant's effective deadline.
Expired grants SHALL fail closed for enforcement surfaces (`grant check` when the
only evaluable grants are expired; `isolator run`). Expire evaluation MUST NOT claim
Isolator OS shipped or set Isolator `sandbox=true`.

#### Scenario: expired grant fails closed
- **WHEN** a grant has `expires_at` in the past
- **THEN** expire evaluation reports expired/fail and enforcement surfaces refuse
  the grant (fail closed)

#### Scenario: ttl_seconds past fails closed
- **WHEN** a grant has `ttl_seconds` and the evaluation clock is past the effective
  deadline derived from that TTL
- **THEN** expire evaluation reports expired/fail (fail closed)
